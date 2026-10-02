package com.example.doc_intel.Service;

import com.example.doc_intel.ChatModels.StreamChatModel.StreamChatModelClient;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.DocumentProcesser.DocumentProcessor;
import com.example.doc_intel.DTO.DocumentsDTO.DocumentResponseDTO;
import com.example.doc_intel.DTO.DocumentsDTO.DocumentSummeryResponse;
import com.example.doc_intel.DTO.UserDTOs.UserDocumentsResponseDTO;
import com.example.doc_intel.EmbedingStore.EmbeddingRequestHandler;
import com.example.doc_intel.Entity.AIConfig;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Enums.ChatDataType;
import com.example.doc_intel.Enums.DocumentStatus;
import com.example.doc_intel.Exceptions.ChatModelExceptions.AIConfigNotExistException;
import com.example.doc_intel.Exceptions.DBExceptions.DocumentNotExistException;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.MinIOProcesser.MinIOProcessor;
import com.example.doc_intel.Repository.AIConfigRepository;
import com.example.doc_intel.Repository.DocumentsRepository;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Utils.Utils;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Service
@Slf4j
@AllArgsConstructor
public class DocumentService {

    private final MinIOProcessor minIOProcessor;

    private final UserRepository userRepository;

    private final DocumentsRepository documentsRepository;

    private final PublisherService publisherService;

    private final AIConfigRepository aiConfigRepository;

    private final StreamChatModelClient streamChatModelClient;

    private final EmbeddingRequestHandler embeddingRequestHandler;

    private final DocumentProcessor documentProcessor;

    /**
     * Upload File to MinIO and Store Embedding in the Vector Store
     */
    public List<DocumentResponseDTO> uploadFile(@NonNull List<MultipartFile> files) {
        //---------------- User Check
        String email = Utils.getUserEmail();
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUserEntity.isEmpty()) {
            log.info("User Not Exist");
            throw new UserNotExistException("User not Exist");
        }
        UserEntity userEntity = optionalUserEntity.get();
        List<KafkaEventDTO> kafkaEventDTOS = new ArrayList<>();
        List<DocumentResponseDTO> documentResponseDTOS = documentProcessor.processDocuments(userEntity, files,
            kafkaEventDTOS);
        kafkaEventDTOS.forEach(publisherService::publishDocument);
        return documentResponseDTOS;
    }

    /**
     * Update File to MinIO and Store Embedding in the Vector Store
     */
    public DocumentResponseDTO updateDocument(@NonNull UUID documentId,
                                              @NonNull MultipartFile file) {
        String email = Utils.getUserEmail();
        List< KafkaEventDTO> kafkaEventDTO = new ArrayList<>();
        DocumentResponseDTO documentResponseDTO = documentProcessor.processDocumentUpdate(email, documentId, file, kafkaEventDTO);
        publisherService.publishDocument(kafkaEventDTO.getFirst());
        return documentResponseDTO;
    }

    /**
     * Soft Delete Document from the Table and Vector Store
     */
    @Transactional
    public DocumentResponseDTO deleteDocument(@NonNull UUID documentId) {
        String email = Utils.getUserEmail();
        // User Check
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUserEntity.isEmpty()) {
            log.info("User Email: {} not exist", email);
            throw new UserNotExistException("User not Exist");
        }

        // Document Check
        Optional<DocumentEntity> optionalDocumentEntity = documentsRepository.findByDocumentIdAndIsActiveTrue(
            documentId);
        if (optionalDocumentEntity.isEmpty()) {
            log.info("Document Id: {} not exist", documentId);
            throw new DocumentNotExistException("Document Id: %s not exist".formatted(documentId));
        }
        DocumentEntity documentEntity = optionalDocumentEntity.get();

        // Soft Deleting Document in the Table
        documentEntity.setIsActive(false);
        documentEntity.setStatus(DocumentStatus.DELETED);

        return DocumentResponseDTO.builder()
            .version(documentEntity.getVersion())
            .URI(null)
            .documentId(documentEntity.getDocumentId())
            .chunks(documentEntity.getChunks())
            .fileExtensions(documentEntity.getFileExtensions())
            .fileName(documentEntity.getFileName())
            .fileSize(documentEntity.getFileSize())
            .createdAt(documentEntity.getCreatedAt())
            .updatedAt(documentEntity.getUpdateAt())
            .build();
    }

    /**
     * Get All Documents of the User
     */
    @Transactional
    public UserDocumentsResponseDTO getUserAllDocuments() {
        String email = Utils.getUserEmail();
        // User Check
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUserEntity.isEmpty()) {
            log.info("User Email: {} not exist", email);
            throw new UserNotExistException("User not Exist");
        }

        // Documen Check
        UserEntity userEntity = optionalUserEntity.get();
        List<DocumentEntity> documentEntity = documentsRepository.findByUser_EmailAndIsActiveTrue(email);
        if (documentEntity.isEmpty()) {
            log.info("No Documents Found");
        }

        List<DocumentResponseDTO> documents = documentEntity.stream().map(
            document -> {
                // get Persistence URI;
                String url = minIOProcessor.getPresignedObjectUrl(document.getObjectKey(), document.getContentType(),
                    document.getFileName());

                return DocumentResponseDTO.builder()
                    .fileName(document.getFileName())
                    .fileSize(document.getFileSize())
                    .fileExtensions(document.getFileExtensions())
                    .documentId(document.getDocumentId())
                    .URI(url)
                    .version(document.getVersion())
                    .createdAt(document.getCreatedAt())
                    .updatedAt(document.getUpdateAt())
                    .status(document.getStatus())
                    .chunks(document.getChunks())
                    .build();
            }).toList();

        log.info("Documents Found: {}", documents.size());
        return UserDocumentsResponseDTO.builder()
            .userId(userEntity.getUserId())
            .username(userEntity.getUsername())
            .firstName(userEntity.getFirstName())
            .lastName(userEntity.getLastName())
            .email(userEntity.getEmail())
            .documents(documents)
            .build();
    }

    /**
     * Get Document of the User with DocumentId
     */
    @Transactional
    public DocumentResponseDTO getDocument(@NonNull UUID documentId) {
        String email = Utils.getUserEmail();
        // User Check
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUserEntity.isEmpty()) {
            log.info("User Email: {} not exist", email);
            throw new UserNotExistException("User not Exist");
        }

        // Document Check
        UserEntity userEntity = optionalUserEntity.get();
        Optional<DocumentEntity> optionalDocumentEntity = documentsRepository.findByDocumentIdAndIsActiveTrue(
            documentId);
        if (optionalDocumentEntity.isEmpty()) {
            log.info("No Documents Found");
            throw new DocumentNotExistException("No Documents Found");
        }

        DocumentEntity documentEntity = optionalDocumentEntity.get();
        String url = minIOProcessor.getPresignedObjectUrl(documentEntity.getObjectKey(),
            documentEntity.getContentType(), documentEntity.getFileName());

        return DocumentResponseDTO.builder()
            .fileName(documentEntity.getFileName())
            .fileSize(documentEntity.getFileSize())
            .fileExtensions(documentEntity.getFileExtensions())
            .documentId(documentEntity.getDocumentId())
            .URI(url)
            .version(documentEntity.getVersion())
            .createdAt(documentEntity.getCreatedAt())
            .updatedAt(documentEntity.getUpdateAt())
            .status(documentEntity.getStatus())
            .chunks(documentEntity.getChunks())
            .build();
    }

    /**
     * Get Document Summery of the User with DocumentId
     */
    @Transactional
    public ResponseBodyEmitter getDocumentSummery(@NonNull UUID documentId) {
        String email = Utils.getUserEmail();

        // User At-least exist
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUserEntity.isEmpty()) {
            log.info("User Email: {} not exist", email);
            throw new UserNotExistException("User not Exist");
        }
        UserEntity userEntity = optionalUserEntity.get();

        // Document should also exist
        Optional<DocumentEntity> optionalDocumentEntity = documentsRepository.findByDocumentIdAndIsActiveTrue(
            documentId);
        if (optionalDocumentEntity.isEmpty()) {
            log.info("No Documents Found");
            throw new DocumentNotExistException("No Documents Found");
        }
        DocumentEntity documentEntity = optionalDocumentEntity.get();

        // AI Config Should Exist.
        Optional<AIConfig> aiConfigOptional = aiConfigRepository.findByUser_Email(email);
        if (aiConfigOptional.isEmpty()) {
            throw new AIConfigNotExistException("AI configuration not found");
        }
        AIConfig aiConfig = aiConfigOptional.get();
        StreamingChatModel chatModel = streamChatModelClient.giveMeModel(aiConfig);

        // Create Filter to Open Search to get the context
        Filter filter = metadataKey(Constants.META_USER_ID).isEqualTo(userEntity.getUserId());

        // Document Handling and Create Filter for the latest document
        filter = filter.and(metadataKey(Constants.META_DOCUMENT_ID).isEqualTo(documentId))
            .and(metadataKey(Constants.META_DOCUMENT_VERSION).isEqualTo(documentEntity.getVersion()));

        EmbeddingSearchResult<TextSegment> searchResultContext = embeddingRequestHandler.makeFilterRequest(filter);

        // 6. Create RAG prompt
        final String context = Utils.createContext(searchResultContext.matches());
        final String prompt =
            Constants.PROMPT.formatted(context, Constants.INTERNAL_QUESTION);

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(10 * 60 * 1000L);
        chatModel.chat(prompt, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String partialResponse) {
                    DocumentSummeryResponse documentSummeryResponse = DocumentSummeryResponse.builder()
                        .type(ChatDataType.CHUNK.name())
                        .data(partialResponse)
                        .success(false)
                        .error(false).build();
                    Utils.sendResponse(emitter, documentSummeryResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    DocumentSummeryResponse documentSummeryResponse = DocumentSummeryResponse.builder()
                        .type(ChatDataType.COMPLETED.name())
                        .data(null)
                        .success(true)
                        .error(false)
                        .build();
                    Utils.sendResponse(emitter, documentSummeryResponse);
                    emitter.complete();
                }

                @Override
                public void onError(Throwable error) {
                    log.error("LLM streaming Error: {}", error.getMessage());
                    DocumentSummeryResponse documentSummeryResponse = DocumentSummeryResponse.builder()
                        .type(ChatDataType.ERROR.name())
                        .data("Unable to generate response")
                        .success(false)
                        .error(true)
                        .build();
                    Utils.sendResponse(emitter, documentSummeryResponse);
                    emitter.complete();
                }
            }
        );
        return emitter;
    }
}
