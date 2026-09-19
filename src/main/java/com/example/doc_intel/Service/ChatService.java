package com.example.doc_intel.Service;

import com.example.doc_intel.ChatModels.StreamChatModel.StreamChatModelClient;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.ChatModel.ChatStreamResponse;
import com.example.doc_intel.DTO.TextSegmentResponseDTO;
import com.example.doc_intel.Entity.AIConfig;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Enums.ChatDataType;
import com.example.doc_intel.Enums.StoreType;
import com.example.doc_intel.Exceptions.UnAuthenticatedUser;
import com.example.doc_intel.Repository.AIConfigRepository;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Store.StoreFactory;
import com.example.doc_intel.Utils.Utils;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Slf4j
@Component
public class ChatService {

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final StreamChatModelClient streamChatModelClient;

    private final AIConfigRepository aiConfigRepository;

    private final UserRepository userRepository;

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    public ChatService(StreamChatModelClient streamChatModelClient,
                       AIConfigRepository aiConfigRepository,
                       UserRepository userRepository,
                       StoreFactory storeFactory,
                       @Value("${vector.data.store}") final StoreType storeType) {
        this.streamChatModelClient = streamChatModelClient;
        this.aiConfigRepository = aiConfigRepository;
        this.userRepository = userRepository;
        this.embeddingStore = storeFactory.giveMeStore(storeType).giveMeStore();
    }


    public ResponseBodyEmitter chat(@NotBlank String query, @Nullable String documentId) {
        String email = Utils.getUserEmail();
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new UnAuthenticatedUser("Unauthenticated user");
        }
        UserEntity userEntity = optionalUser.get();


        Optional<AIConfig> aiConfigOptional = aiConfigRepository.findByUser_Email(email);
        if (aiConfigOptional.isEmpty()) {
            throw new UnAuthenticatedUser("No Config Found");
        }
        AIConfig aiConfig = aiConfigOptional.get();

        StreamingChatModel chatModel = streamChatModelClient.giveMeModel(aiConfig);

        List<TextSegmentResponseDTO> textSegmentResponseDTO = new ArrayList<>();

        // 2. Convert question into embedding
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // Created Search Filter
        Filter filter = metadataKey(Constants.META_USER_ID).isEqualTo(userEntity.getUserId());
        if (Objects.nonNull(documentId)) {
            filter = filter.and(metadataKey(Constants.META_DOCUMENT_ID).isEqualTo(documentId));
        }

        // 3. Search OpenSearch
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(5)
            .minScore(0.5)
            .filter(filter)
            .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(request);

        // 4. Get relevant chunks
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
        log.info("Retrieved chunks: {}", matches.size());

        // 5. Build context
        String context = matches.stream()
            .map(match -> {
                TextSegment segment = match.embedded();
                Metadata metadata = segment.metadata();
                String fileName = metadata.getString(Constants.META_DATA_FILE_NAME);
                Integer pageNumber = metadata.getInteger(Constants.META_DATA_PAGE_NUMBER);
                Integer lineNumber = metadata.getInteger(Constants.META_DATA_LINE_NUMBER);
                String docId = metadata.getString(Constants.META_DOCUMENT_ID);
                String text = segment.text();
                double score = match.score() * 100;
                String version = metadata.getString(Constants.META_DOCUMENT_VERSION);
                textSegmentResponseDTO.add(
                    TextSegmentResponseDTO.builder()
                        .fileName(fileName)
                        .pageNumber(pageNumber)
                        .lineNumber(lineNumber)
                        .text(text)
                        .version(version)
                        .documentId(docId)
                        .score(String.format("%.2f%%", score))
                        .build());
                return match.embedded().text();
            })
            .collect(Collectors.joining("\n\n"));

        // 6. Create RAG prompt
        final String prompt = Constants.PROMPT.formatted(context, query);

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(10 * 60 * 1000L);
        chatModel.chat(prompt, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String partialResponse) {
                    ChatStreamResponse chatStreamResponse = ChatStreamResponse.builder()
                        .type(ChatDataType.CHUNK.name())
                        .data(partialResponse)
                        .success(false)
                        .error(false)
                        .textSegmentResponseDTO(null).build();
                    sendResponse(emitter, chatStreamResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    ChatStreamResponse chatStreamResponse = ChatStreamResponse.builder()
                        .type(ChatDataType.COMPLETED.name())
                        .data(null)
                        .success(true)
                        .error(false)
                        .textSegmentResponseDTO(textSegmentResponseDTO).build();
                    sendResponse(emitter, chatStreamResponse);
                    emitter.complete();
                }

                @Override
                public void onError(Throwable error) {
                    ChatStreamResponse chatStreamResponse = ChatStreamResponse.builder()
                        .type(ChatDataType.ERROR.name())
                        .data("Unable to generate response")
                        .success(false)
                        .error(true)
                        .textSegmentResponseDTO(null).build();
                    sendResponse(emitter, chatStreamResponse);
                    emitter.complete();
                }
            }
        );
        return emitter;
    }

    private void sendResponse(ResponseBodyEmitter emitter, ChatStreamResponse response) {
        try {
            emitter.send(SseEmitter.event().data(response).build());
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
