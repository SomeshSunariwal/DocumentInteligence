package com.example.doc_intel.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.stereotype.Component;

import com.example.doc_intel.ChatModels.LongChainChatModel.ChatModelFactory;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.ChatModel.AISearchResponseDTO;
import com.example.doc_intel.DTO.ChatModel.SearchResponseDTO;
import com.example.doc_intel.DTO.TextSegmentResponseDTO;
import com.example.doc_intel.EmbedingStore.EmbeddingRequestHandler;
import com.example.doc_intel.Entity.AIConfig;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Exceptions.ChatModelExceptions.AIConfigNotExistException;
import com.example.doc_intel.Exceptions.ChatModelExceptions.NoResultFoundException;
import com.example.doc_intel.Exceptions.ProcessFileException;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.Repository.AIConfigRepository;
import com.example.doc_intel.Repository.DocumentsRepository;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Utils.Utils;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class SearchService {

    private final ChatModelFactory chatModelFactory;

    private final AIConfigRepository aiConfigRepository;

    private final UserRepository userRepository;

    private final DocumentsRepository documentsRepository;

    private final EmbeddingRequestHandler embeddingRequestHandler;

    public SearchResponseDTO processSearch(@NotBlank String query) {
        String email = Utils.getUserEmail();
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUserEntity.isEmpty()) {
            throw new UserNotExistException("User Not Exist");
        }
        UserEntity userEntity = optionalUserEntity.get();
        List<TextSegmentResponseDTO> textSegmentResponseDTO = new ArrayList<>();

        // Created Search Filter
        Filter filter = metadataKey(Constants.META_USER_ID).isEqualTo(userEntity.getUserId());

        // Handing OpenSearch Request
        EmbeddingSearchResult<TextSegment> searchResult
            = embeddingRequestHandler.makeRequest(query, filter, 5);

        // 4. Get relevant chunks
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
        log.info("Retrieved chunks: {}", matches.size());

        // 5. Build context
        createTextSegmentResponse(matches, textSegmentResponseDTO);
        log.info("Search Response Generated");
        return SearchResponseDTO.builder()
            .textSegmentResponseDTOList(textSegmentResponseDTO)
            .build();
    }

    public AISearchResponseDTO processAISearch(@Nullable UUID documentId, @NotBlank String query) {
        String email = Utils.getUserEmail();
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(email);
        if (optionalUserEntity.isEmpty()) {
            throw new UserNotExistException("User Not Exist");
        }
        UserEntity userEntity = optionalUserEntity.get();

        List<TextSegmentResponseDTO> textSegmentResponseDTO = new ArrayList<>();
        Filter filter = metadataKey(Constants.META_USER_ID).isEqualTo(userEntity.getUserId());

        // Document Handling and Create Filter
        if (Objects.nonNull(documentId)) {
            Optional<DocumentEntity> optionalDocumentEntity = documentsRepository
                .findByDocumentIdAndUser_EmailAndIsActiveTrue(documentId, email);

            if (optionalDocumentEntity.isEmpty()) {
                throw new NoResultFoundException("Document Not Found");
            }
            DocumentEntity documentEntity = optionalDocumentEntity.get();
            filter = filter.and(metadataKey(Constants.META_DOCUMENT_ID).isEqualTo(documentId))
                .and(metadataKey(Constants.META_DOCUMENT_VERSION).isEqualTo(documentEntity.getVersion()));
        }

        // Handing OpenSearch Request
        EmbeddingSearchResult<TextSegment> searchResult
            = embeddingRequestHandler.makeRequest(query, filter, 5);

        // 4. Get relevant chunks
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
        log.info("Retrieved chunks: {}", matches.size());

        // 5. Build textSegmentResponseDTO
        createTextSegmentResponse(matches, textSegmentResponseDTO);

        // New Request Filter for more context
        // make new filters to get prev and next chunk form the OpenSearch to get more context
        filter = filter.and(Utils.getExtraContextFromOpenSearch(matches));
        EmbeddingSearchResult<TextSegment> searchResultContext
            = embeddingRequestHandler.makeFilterRequest(filter);

        // 6. Create RAG prompt
        String context = Utils.createContext(searchResultContext.matches());
        final String prompt = Constants.PROMPT.formatted(context, query);
        ChatModel chatModel = getChatModel(email);
        try {
            String answer = chatModel.chat(prompt);
            log.info("AI Response Generated");
            return AISearchResponseDTO.builder()
                .result(answer)
                .textSegmentResponseDTOList(textSegmentResponseDTO)
                .build();
        } catch (RuntimeException e) {
            log.error("Chat model request failed", e);
            throw new ProcessFileException("Unable to generate an AI response", e);
        }
    }

    private ChatModel getChatModel(@NotBlank String email) {
        Optional<AIConfig> optionalAIConfig = aiConfigRepository.findByUser_Email(email);
        if (optionalAIConfig.isEmpty()) {
            throw new AIConfigNotExistException("AI Config Not Exist");
        }
        AIConfig aiConfig = optionalAIConfig.get();
        return chatModelFactory.giveMeChatModel(aiConfig.getType()).giveMeModel(aiConfig);
    }

    public void createTextSegmentResponse(final List<EmbeddingMatch<TextSegment>> matches,
                                          List<TextSegmentResponseDTO> textSegmentResponseDTO) {

        matches.forEach(match -> {
            TextSegment segment = match.embedded();
            Metadata metadata = segment.metadata();
            String fileName = metadata.getString(Constants.META_DATA_FILE_NAME);
            Integer pageNumber = metadata.getInteger(Constants.META_DATA_PAGE_NUMBER);
            Integer lineNumber = metadata.getInteger(Constants.META_DATA_LINE_NUMBER);
            String docId = metadata.getString(Constants.META_DOCUMENT_ID);
            String text = segment.text();
            double score = match.score() * 100;
            Integer version = metadata.getInteger(Constants.META_DOCUMENT_VERSION);
            String userId = metadata.getString(Constants.META_USER_ID);
            textSegmentResponseDTO.add(
                TextSegmentResponseDTO.builder()
                    .fileName(fileName)
                    .pageNumber(pageNumber)
                    .lineNumber(lineNumber)
                    .text(text)
                    .userId(userId)
                    .version(version)
                    .documentId(docId)
                    .score(String.format("%.2f%%", score))
                    .build());
        });
    }


}
