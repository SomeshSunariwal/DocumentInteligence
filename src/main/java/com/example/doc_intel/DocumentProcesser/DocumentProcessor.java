package com.example.doc_intel.DocumentProcesser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.DTO.DocumentsDTO.DocumentResponseDTO;
import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Entity.DocumentVersionEntity;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Enums.DocumentStatus;
import com.example.doc_intel.Enums.FileExtensions;
import com.example.doc_intel.Exceptions.DBExceptions.DocumentNotExistException;
import com.example.doc_intel.Exceptions.UnSupportedFileException;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.MinIOProcesser.MinIOProcessor;
import com.example.doc_intel.Repository.DocumentsRepository;
import com.example.doc_intel.Repository.DocumentVersionsRepository;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Utils.Utils;

import io.minio.ObjectWriteResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class DocumentProcessor {

    private final MinIOProcessor minIOProcessor;

    private final UserRepository userRepository;

    private final DocumentsRepository documentsRepository;

    private final DocumentVersionsRepository documentVersionsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<DocumentResponseDTO> processUploadDocuments(@NonNull UserEntity user, @NonNull List<MultipartFile> files,
                                                      @NonNull List<KafkaEventDTO> kafkaEventDTOS) {
        List<DocumentResponseDTO> documentResponseDTOS = new ArrayList<>();
        for (MultipartFile file : files) {
            documentResponseDTOS.add(processOneDocument(user, file, kafkaEventDTOS));
        }
        return documentResponseDTOS;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DocumentResponseDTO processDocumentUpdate(@NonNull String email, @NonNull UUID documentId,
                                                     @NonNull MultipartFile file,
                                                     @NonNull List<KafkaEventDTO> kafkaEventDTOS) {
        FileExtensions fileExtension = Utils.getExtension(file);
        if (fileExtension == null) {
            throw new UnSupportedFileException("File Type not support", ErrorCode.DocumentUpdateUnsupportedFile);
        }

        Optional<UserEntity> userOptional = userRepository.findByEmailAndIsActiveTrue(email);
        if (userOptional.isEmpty()) {
            throw new UserNotExistException("User not Exist", ErrorCode.DocumentProcessorUserNotFound);
        }
        Optional<DocumentEntity> documentOptional = documentsRepository
            .findForUpdateByDocumentIdAndUser_EmailAndIsActiveTrue(documentId, email);
        if (documentOptional.isEmpty()) {
            throw new DocumentNotExistException("Document Id: %s not exist".formatted(documentId),
                ErrorCode.DocumentUpdateTargetNotFound);
        }

        UserEntity user = userOptional.get();
        DocumentEntity document = documentOptional.get();
        DocumentVersionEntity previousVersion = documentVersionsRepository
            .findFirstByDocument_DocumentIdAndDocument_IsActiveTrueOrderByDocumentVersionDesc(documentId)
            .orElseThrow(() -> new DocumentNotExistException("Document version not found",
                ErrorCode.DocumentUpdateVersionNotFound));

        String fileName = file.getOriginalFilename() == null
            ? "Document.%s".formatted(fileExtension) : file.getOriginalFilename();
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();

        ObjectWriteResponse writeResponse = minIOProcessor.putObject(file, previousVersion.getObjectKey());

        LocalDateTime now = LocalDateTime.now();
        document.setUpdateAt(now);
        document.setUpdatedBy(email);
        DocumentVersionEntity version = DocumentVersionEntity.builder()
            .document(document)
            .fileName(fileName)
            .objectKey(previousVersion.getObjectKey())
            .bucketName(previousVersion.getBucketName())
            .contentType(contentType)
            .fileSize(file.getSize())
            .fileExtensions(fileExtension)
            .chunksCount(0)
            .documentVersion(previousVersion.getDocumentVersion() + 1)
            .minIOVersionId(writeResponse.versionId())
            .createdAt(now)
            .createdBy(email)
            .status(DocumentStatus.UPLOADED)
            .build();
        DocumentVersionEntity savedVersion = documentVersionsRepository.save(version);

        kafkaEventDTOS.add(createEvent(user, savedVersion));
        return toResponse(document, savedVersion);
    }

    private DocumentResponseDTO processOneDocument(UserEntity user, MultipartFile file,
                                                   List<KafkaEventDTO> events) {
        FileExtensions fileExtension = Utils.getExtension(file);
        if (fileExtension == null) {
            throw new UnSupportedFileException("File Type not support", ErrorCode.DocumentUploadUnsupportedFile);
        }
        String fileName = file.getOriginalFilename() == null
            ? "Document.%s".formatted(fileExtension) : file.getOriginalFilename();
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();

        UUID documentId = UUID.randomUUID();
        String objectKey = Utils.getObjectKey(user.getUsername(), documentId, fileName);
        ObjectWriteResponse writeResponse = minIOProcessor.putObject(file, objectKey);
        log.info("Version Created with : {}", writeResponse.versionId());

        LocalDateTime now = LocalDateTime.now();
        DocumentEntity document = DocumentEntity.builder()
            .documentId(documentId)
            .isActive(true)
            .user(user)
            .createdAt(now)
            .createdBy(user.getEmail())
            .updateAt(now)
            .updatedBy(user.getEmail())
            .build();
        DocumentEntity savedDocument = documentsRepository.save(document);

        DocumentVersionEntity documentVersionEntity = DocumentVersionEntity.builder()
            .document(savedDocument)
            .fileName(fileName)
            .objectKey(objectKey)
            .bucketName(Constants.MINIO_BUCKET_NAME)
            .contentType(contentType)
            .fileSize(file.getSize())
            .fileExtensions(fileExtension)
            .chunksCount(0)
            .documentVersion(1)
            .minIOVersionId(writeResponse.versionId())
            .createdAt(now)
            .createdBy(user.getEmail())
            .status(DocumentStatus.UPLOADED)
            .build();

        DocumentVersionEntity savedVersion = documentVersionsRepository.save(documentVersionEntity);
        events.add(createEvent(user, savedVersion));
        return toResponse(savedDocument, savedVersion);
    }

    private KafkaEventDTO createEvent(UserEntity user, DocumentVersionEntity documentVersionEntity) {
        return KafkaEventDTO.builder()
            .eventId(UUID.randomUUID())
            .documentId(documentVersionEntity.getDocument().getDocumentId())
            .userId(user.getUserId())
            .objectKey(documentVersionEntity.getObjectKey())
            .fileName(documentVersionEntity.getFileName())
            .fileExtensions(documentVersionEntity.getFileExtensions())
            .documentVersion(documentVersionEntity.getDocumentVersion())
            .minIOVersion(documentVersionEntity.getMinIOVersionId())
            .build();
    }

    private DocumentResponseDTO toResponse(DocumentEntity document, DocumentVersionEntity version) {
        return DocumentResponseDTO.builder()
            .version(version.getDocumentVersion())
            .URI(null)
            .documentId(document.getDocumentId())
            .fileName(version.getFileName())
            .fileSize(version.getFileSize())
            .fileExtensions(version.getFileExtensions())
            .chunks(version.getChunksCount())
            .createdAt(version.getCreatedAt())
            .updatedAt(document.getUpdateAt())
            .status(version.getStatus())
            .build();
    }
}
