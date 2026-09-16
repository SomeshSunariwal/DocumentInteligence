package com.example.doc_intel.Service;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.DocumentsDTO.DocumentResponseDTO;
import com.example.doc_intel.DTO.UserDTOs.UserDocumentsResponseDTO;
import com.example.doc_intel.DocumentEncoder.DocumentEncoder;
import com.example.doc_intel.DocumentEncoder.DocumentEncoderFactory;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Enums.DocumentStatus;
import com.example.doc_intel.Enums.FileExtensions;
import com.example.doc_intel.Enums.StoreType;
import com.example.doc_intel.Exceptions.*;
import com.example.doc_intel.Exceptions.DBExceptions.DocumentNotExistException;
import com.example.doc_intel.MinIOProcesser.MinIOProcessor;
import com.example.doc_intel.Repository.DocumentsRepository;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Store.StoreFactory;
import com.example.doc_intel.Utils.Utils;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.minio.ObjectWriteResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class DocumentService {

    private final MinIOProcessor minIOProcessor;

    private final UserRepository userRepository;

    private final DocumentsRepository documentsRepository;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final DocumentEncoderFactory documentEncoderFactory;

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    private DocumentEncoder documentEncoder;

    DocumentService(@Value("${vector.data.store}") final StoreType storeType,
                    MinIOProcessor minIOProcessor,
                    UserRepository userRepository,
                    DocumentsRepository documentsRepository,
                    StoreFactory storeFactory,
                    DocumentEncoderFactory documentEncoderFactory) {
        this.minIOProcessor = minIOProcessor;
        this.userRepository = userRepository;
        this.documentsRepository = documentsRepository;
        this.embeddingStore = storeFactory.giveMeStore(storeType).giveMeStore();
        this.documentEncoderFactory = documentEncoderFactory;
    }

    /**
     * Upload File to MinIO and Store Embedding in the Vector Store
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<DocumentResponseDTO> uploadFile(@NonNull List<MultipartFile> files) {
        //---------------- User Check
        String email = Utils.getUserEmail();
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUserEntity.isEmpty()) {
            log.info("User Not Exist");
            throw new UserNotExistException("User not Exist");
        }
        UserEntity userEntity = optionalUserEntity.get();
        return files.stream()
            .map(file -> processDocument(userEntity, file))
            .toList();
    }

    /**
     * Update File to MinIO and Store Embedding in the Vector Store
     */
    @Transactional
    public DocumentResponseDTO updateDocument(@NonNull UUID documentId,
                                              @NonNull MultipartFile file) {
        String email = Utils.getUserEmail();
        FileExtensions fileExtension = Utils.getExtension(file);
        if (Objects.isNull(fileExtension)) {
            throw new UnSupportedFileException("File Type not support");
        }
        // User Check
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUserEntity.isEmpty()) {
            log.info("User Email: {} not exist", email);
            throw new UserNotExistException("User not Exist");
        }

        // Documen Check
        DocumentEntity documentEntity = documentsRepository.findByDocumentIdAndIsActiveTrue(documentId);
        if (Objects.isNull(documentEntity)) {
            log.info("Document Id: {} not exist", documentId);
            throw new DocumentNotExistException("Document Id: %s not exist".formatted(documentId));
        }

        String fileName = Objects.isNull(file.getOriginalFilename()) ? "Document.%s".formatted(fileExtension) :
            file.getOriginalFilename();
        String contentType = Objects.isNull(file.getContentType()) ? "application/octet-stream" : file.getContentType();

        // ---------------------- Document Encoding and Storing Embedding
        Integer chunks = StoreEmbeddings(file, fileExtension, optionalUserEntity.get().getUserId().toString(),
            documentEntity.getVersion() + 1, documentId.toString());

        // Updated Document in the Table
        String objectKey = documentEntity.getObjectKey();
        documentEntity.setFileName(fileName);
        documentEntity.setFileSize(file.getSize());
        documentEntity.setContentType(contentType);
        documentEntity.setUpdateAt(LocalDateTime.now());
        documentEntity.setUpdatedBy(email);
        documentEntity.setStatus(DocumentStatus.UPLOADED);
        documentEntity.setVersion(documentEntity.getVersion() + 1);
        documentEntity.setChunks(chunks);

        // Upload File to MinIO
        ObjectWriteResponse objectWriteResponse = minIOProcessor.putObject(file, objectKey);
        log.info("Version Updated to : {}", objectWriteResponse.versionId());

        return DocumentResponseDTO.builder()
            .version(documentEntity.getVersion())
            .URI(null)
            .documentId(documentEntity.getDocumentId())
            .fileName(file.getOriginalFilename())
            .chunks(documentEntity.getChunks())
            .createdAt(documentEntity.getCreatedAt())
            .updatedAt(documentEntity.getUpdateAt())
            .status(documentEntity.getStatus())
            .build();
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
        DocumentEntity documentEntity = documentsRepository.findByDocumentIdAndIsActiveTrue(documentId);
        if (Objects.isNull(documentEntity)) {
            log.info("Document Id: {} not exist", documentId);
            throw new DocumentNotExistException("Document Id: %s not exist".formatted(documentId));
        }
        // Soft Deleting Document in the Table
        documentEntity.setIsActive(false);
        documentEntity.setStatus(DocumentStatus.DELETED);

        return DocumentResponseDTO.builder()
            .version(documentEntity.getVersion())
            .URI(null)
            .documentId(documentEntity.getDocumentId())
            .chunks(documentEntity.getChunks())
            .fileName(documentEntity.getFileName())
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
                String url = minIOProcessor.getPresignedObjectUrl(document.getObjectKey());
                return DocumentResponseDTO.builder()
                    .fileName(document.getFileName())
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
     * Store Embeddings in the Vector Store
     */
    private Integer StoreEmbeddings(@NonNull MultipartFile file, @NonNull FileExtensions fileExtension,
                                    @NonNull String userId, @NonNull Integer version, @NonNull String documentId) {
        documentEncoder = documentEncoderFactory.getParser(fileExtension);
        List<TextSegment> chunks;

        try {
            chunks = documentEncoder.encode(file, userId, version, documentId);
        } catch (Exception e) {
            throw new ProcessFileException("Internal Server Error");
        }

        log.info("Number of chunks: {}", chunks.size());
        for (TextSegment chunk : chunks) {
            Embedding embedding = embeddingModel.embed(chunk).content();
            embeddingStore.add(embedding, chunk);
        }
        return chunks.size();
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

        // Documen Check
        UserEntity userEntity = optionalUserEntity.get();
        Optional<DocumentEntity> optionalDocumentEntity = documentsRepository.findByDocumentId(documentId);
        if (optionalDocumentEntity.isEmpty()) {
            log.info("No Documents Found");
            throw new DocumentNotExistException("No Documents Found");
        }

        DocumentEntity documentEntity = optionalDocumentEntity.get();
        String url = minIOProcessor.getPresignedObjectUrl(documentEntity.getObjectKey());
        return DocumentResponseDTO.builder()
            .fileName(documentEntity.getFileName())
            .documentId(documentEntity.getDocumentId())
            .URI(url)
            .version(documentEntity.getVersion())
            .createdAt(documentEntity.getCreatedAt())
            .updatedAt(documentEntity.getUpdateAt())
            .status(documentEntity.getStatus())
            .chunks(documentEntity.getChunks())
            .build();
    }

    private DocumentResponseDTO processDocument(@NonNull UserEntity userEntity, @NonNull MultipartFile file) {
        FileExtensions fileExtension = Utils.getExtension(file);
        if (Objects.isNull(fileExtension)) {
            throw new UnSupportedFileException("File Type not support");
        }

        String fileName = Objects.isNull(file.getOriginalFilename()) ? "Document.%s".formatted(fileExtension) :
            file.getOriginalFilename();
        String contentType = Objects.isNull(file.getContentType()) ? "application/octet-stream" : file.getContentType();

        UUID uuid = UUID.randomUUID();
        String objectKey = Utils.getObjectKey(userEntity.getUsername(), uuid, fileName);

        // Upload File to MinIO DataBase
        ObjectWriteResponse objectWriteResponse = minIOProcessor.putObject(file, objectKey);
        log.info("Version Created with : {}", objectWriteResponse.versionId());

        //---------------------- Document Encoding and Storing Embedding
        Integer chunks = StoreEmbeddings(file, fileExtension, userEntity.getUserId().toString(), 1, uuid.toString());

        //---------------------- Placed Document Object Into Table
        DocumentEntity documentEntity = DocumentEntity.builder()
            .documentId(uuid)
            .fileName(file.getOriginalFilename())
            .objectKey(objectKey)
            .bucketName(Constants.MINIO_BUCKET_NAME)
            .contentType(contentType)
            .fileSize(file.getSize())
            .isActive(true)
            .version(1)
            .status(DocumentStatus.UPLOADED)
            .user(userEntity)
            .createdAt(LocalDateTime.now())
            .createdBy(userEntity.getUsername())
            .updateAt(LocalDateTime.now())
            .updatedBy(userEntity.getUsername())
            .chunks(chunks)
            .build();

        //  Update Document Entity with Number of Chunks
        DocumentEntity documentEntityResponse = documentsRepository.save(documentEntity);

        return DocumentResponseDTO.builder()
            .version(documentEntityResponse.getVersion())
            .URI(null)
            .documentId(documentEntityResponse.getDocumentId())
            .fileName(file.getOriginalFilename())
            .chunks(documentEntityResponse.getChunks())
            .createdAt(documentEntity.getCreatedAt())
            .updatedAt(documentEntity.getUpdateAt())
            .status(documentEntity.getStatus())
            .build();
    }
}
