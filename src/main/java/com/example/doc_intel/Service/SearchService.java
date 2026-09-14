package com.example.doc_intel.Service;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.ChatModel.AISearchResponseDTO;
import com.example.doc_intel.DTO.ChatModel.SearchRequestDTO;
import com.example.doc_intel.DTO.ChatModel.SearchResponseDTO;
import com.example.doc_intel.DTO.TextSegmentResponseDTO;
import com.example.doc_intel.Entity.AIConfig;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Enums.StoreType;
import com.example.doc_intel.Exceptions.ChatModelExceptions.AIConfigNotExistException;
import com.example.doc_intel.Exceptions.ChatModelExceptions.NoResultFoundException;
import com.example.doc_intel.Exceptions.MessageLengthException;
import com.example.doc_intel.Exceptions.NullMessageException;
import com.example.doc_intel.Exceptions.ProcessFileException;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.LongChainChatModel.ChatModelFactory;
import com.example.doc_intel.Repository.AIConfigRepository;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Store.StoreFactory;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SearchService {

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final ChatModelFactory chatModelFactory;

    private final AIConfigRepository aiConfigRepository;

    private final UserRepository userRepository;

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    public SearchService(final StoreFactory storeFactory,
                         final ChatModelFactory chatModelFactory,
                         final AIConfigRepository aiConfigRepository,
                         final UserRepository userRepository,
                         @Value("${vector.data.store}") final StoreType storeType) {
        this.embeddingStore = storeFactory.giveMeStore(storeType).giveMeStore();
        this.chatModelFactory = chatModelFactory;
        this.aiConfigRepository = aiConfigRepository;
        this.userRepository = userRepository;
    }


    public SearchResponseDTO processSearch(final String email, final SearchRequestDTO searchRequestDTO) {
        List<TextSegmentResponseDTO> textSegmentResponseDTO = new ArrayList<>();
        String message = searchRequestDTO.getSearchTerm();

        if (Objects.isNull(message)) {
            throw new NullMessageException("Message cannot be null");
        }
        if (message.isEmpty()) {
            throw new MessageLengthException("Please Provide a Question");
        }

        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(email);
        if (optionalUserEntity.isEmpty()) {
            throw new UserNotExistException("User Not Exist");
        }

        // 2. Convert question into embedding
        Embedding queryEmbedding = embeddingModel.embed(message).content();

        // 3. Search OpenSearch
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(5)
            .minScore(0.5)
            .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(request);

        // 4. Get relevant chunks
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
        log.info("Retrieved chunks: {}", matches.size());

        // 5. Build context
        matches.forEach(match -> {
            TextSegment segment = match.embedded();
            Metadata metadata = segment.metadata();
            String fileName = metadata.getString(Constants.META_DATA_FILE_NAME);
            Integer pageNumber = metadata.getInteger(Constants.META_DATA_PAGE_NUMBER);
            Integer lineNumber = metadata.getInteger(Constants.META_DATA_LINE_NUMBER);
            String text = metadata.getString(Constants.META_DATA_TEXT);
            double score = match.score() * 100;
            textSegmentResponseDTO.add(
                new TextSegmentResponseDTO(fileName,
                    pageNumber,
                    lineNumber,
                    text,
                    String.format("%.2f%%", score)));
        });

        try {
            log.info("Response Generated");
            return SearchResponseDTO.builder()
                .textSegmentResponseDTOList(textSegmentResponseDTO)
                .build();
        } catch (NoResultFoundException e) {
            throw new NoResultFoundException("No Result Found, Make Sure Data is Already Fed");
        } catch (Exception e) {
            throw new ProcessFileException("Something Went Wrong With Chat Model");
        }

    }

    public AISearchResponseDTO processAISearch(final String email, final SearchRequestDTO searchRequestDTO) {
        List<TextSegmentResponseDTO> textSegmentResponseDTO = new ArrayList<>();
        String message = searchRequestDTO.getSearchTerm();

        if (Objects.isNull(message)) {
            throw new NullMessageException("Message cannot be null");
        }
        if (message.isEmpty()) {
            throw new MessageLengthException("Please Provide a Question");
        }

        ChatModel chatModel = getChatModel(email);

        // 2. Convert question into embedding
        Embedding queryEmbedding = embeddingModel.embed(message).content();

        // 3. Search OpenSearch
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(5)
            .minScore(0.5)
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
                String text = metadata.getString(Constants.META_DATA_TEXT);
                double score = match.score() * 100;
                textSegmentResponseDTO.add(
                    new TextSegmentResponseDTO(fileName,
                        pageNumber,
                        lineNumber,
                        text,
                        String.format("%.2f%%", score)));
                return match.embedded().text();
            })
            .collect(Collectors.joining("\n\n"));

        // 6. Create RAG prompt
        String prompt = """
            You are a document question-answering assistant.
            
            Answer the question using ONLY the context
            provided below.
            
            If the answer is not present in the context,
            say that you do not know.
            
            CONTEXT:
            %s
            
            QUESTION:
            %s
            
            ANSWER:
            """.formatted(context, searchRequestDTO.getSearchTerm());

        try {
            String answer = chatModel.chat(prompt);
            log.info("Response Generated");
            return AISearchResponseDTO.builder()
                .result(answer)
                .textSegmentResponseDTOList(textSegmentResponseDTO)
                .build();

        } catch (NoResultFoundException e) {
            throw new NoResultFoundException("No Result Found, Make Sure Data is Already Fed");
        } catch (Exception e) {
            throw new ProcessFileException("Something Went Wrong With Chat Model");
        }
    }

    private ChatModel getChatModel(@NotBlank String email) {
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(email);
        if (optionalUserEntity.isEmpty()) {
            throw new UserNotExistException("User Not Exist");
        }
        UserEntity userEntity = optionalUserEntity.get();

        Optional<AIConfig> optionalAIConfig = aiConfigRepository.findByUser_Email(userEntity.getEmail());
        if (optionalAIConfig.isEmpty()) {
            throw new AIConfigNotExistException("AI Config Not Exist");
        }
        AIConfig aiConfig = optionalAIConfig.get();

        return chatModelFactory.giveMeChatModel(aiConfig.getType()).giveMeModel(aiConfig);
    }
}
