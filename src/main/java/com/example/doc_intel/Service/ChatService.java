package com.example.doc_intel.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.example.doc_intel.EmbedingStore.EmbeddingRequestHandler;
import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Entity.DocumentVersionEntity;
import com.example.doc_intel.Exceptions.ChatModelExceptions.NoResultFoundException;
import com.example.doc_intel.Exceptions.ChatModelExceptions.AIConfigNotExistException;
import com.example.doc_intel.Repository.DocumentsRepository;
import com.example.doc_intel.Repository.DocumentVersionsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.example.doc_intel.ChatModels.StreamChatModel.StreamChatModelClient;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.ChatModel.ChatStreamResponse;
import com.example.doc_intel.DTO.TextSegmentResponseDTO;
import com.example.doc_intel.Entity.AIConfig;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Enums.ChatDataType;
import com.example.doc_intel.Exceptions.UnAuthenticatedUser;
import com.example.doc_intel.Repository.AIConfigRepository;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Utils.Utils;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class ChatService {

    private final StreamChatModelClient streamChatModelClient;

    private final AIConfigRepository aiConfigRepository;

    private final UserRepository userRepository;

    private final EmbeddingRequestHandler embeddingRequestHandler;

    private final SearchService searchService;

    private final DocumentsRepository documentsRepository;

    private final DocumentVersionsRepository documentVersionsRepository;

    public ResponseBodyEmitter chat(@NotBlank String query, @Nullable UUID documentId, @Nullable Integer version) {
        String email = Utils.getUserEmail();
        Optional<UserEntity> optionalUser = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUser.isEmpty()) {
            throw new UnAuthenticatedUser("Unauthenticated user", ErrorCode.ChatUserUnauthenticated);
        }
        UserEntity userEntity = optionalUser.get();

        Optional<AIConfig> aiConfigOptional = aiConfigRepository.findByUser_Email(email);
        if (aiConfigOptional.isEmpty()) {
            throw new AIConfigNotExistException("AI configuration not found", ErrorCode.ChatAIConfigMissing);
        }
        AIConfig aiConfig = aiConfigOptional.get();
        StreamingChatModel chatModel = streamChatModelClient.giveMeModel(aiConfig);

        List<TextSegmentResponseDTO> textSegmentResponseDTO = new ArrayList<>();

        // Created Search Filter
        Filter filter = metadataKey(Constants.META_USER_ID).isEqualTo(userEntity.getUserId());
        if (Objects.nonNull(documentId)) {
            Optional<DocumentEntity> optionalDocumentEntity =
                documentsRepository.findByDocumentIdAndUser_EmailAndIsActiveTrue(documentId, email);
            if (optionalDocumentEntity.isEmpty()) {
                throw new NoResultFoundException("Document Not Found", ErrorCode.ChatTargetNotFound);
            }
            int latestVersion = 0;
            // If no version is provided in the request then latest version will be picked
            DocumentVersionEntity documentVersionEntity;
            if (Objects.isNull(version)) {
                documentVersionEntity = documentVersionsRepository
                    .findFirstByDocument_DocumentIdAndDocument_IsActiveTrueOrderByDocumentVersionDesc(documentId)
                    .orElseThrow(() -> new NoResultFoundException("There are no document versions",
                        ErrorCode.ChatLatestVersionNotFound));
            } else {
                documentVersionEntity = documentVersionsRepository
                    .findByDocument_DocumentIdAndDocumentVersionAndDocument_IsActiveTrue(documentId, version)
                    .orElseThrow(() -> new NoResultFoundException("Document Version Not Found",
                        ErrorCode.ChatRequestedVersionNotFound));
            }
            latestVersion = documentVersionEntity.getDocumentVersion();
            filter = filter.and(metadataKey(Constants.META_DOCUMENT_ID).isEqualTo(documentId))
                .and(metadataKey(Constants.META_DOCUMENT_VERSION).isEqualTo(latestVersion));
        }

        EmbeddingSearchResult<TextSegment> searchResult =
            embeddingRequestHandler.makeRequest(query, filter, Constants.MAX_EMBEDDING_RESULT);

        // 4. Get relevant chunks
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
        log.info("Retrieved chunks: {}", matches.size());

        // 5. Build context
        searchService.createTextSegmentResponse(matches, textSegmentResponseDTO);

        // Get Next and Previous Context from Open Search
        filter = filter.and(Utils.getExtraContextFromOpenSearch(matches));
        EmbeddingSearchResult<TextSegment> searchResultContext =
            embeddingRequestHandler.makeFilterRequest(filter);

        // 6. Create RAG prompt
        String context = Utils.createContext(searchResultContext.matches());
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
                    Utils.sendResponse(emitter, chatStreamResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    ChatStreamResponse chatStreamResponse = ChatStreamResponse.builder()
                        .type(ChatDataType.COMPLETED.name())
                        .data(null)
                        .success(true)
                        .error(false)
                        .textSegmentResponseDTO(textSegmentResponseDTO).build();
                    Utils.sendResponse(emitter, chatStreamResponse);
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
                    Utils.sendResponse(emitter, chatStreamResponse);
                    emitter.complete();
                }
            }
        );
        return emitter;
    }
}
