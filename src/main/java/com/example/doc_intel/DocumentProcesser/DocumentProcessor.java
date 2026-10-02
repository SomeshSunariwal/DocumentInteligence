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
import com.example.doc_intel.DTO.DocumentsDTO.DocumentResponseDTO;
import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Enums.DocumentStatus;
import com.example.doc_intel.Enums.FileExtensions;
import com.example.doc_intel.Exceptions.DBExceptions.DocumentNotExistException;
import com.example.doc_intel.Exceptions.UnSupportedFileException;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.MinIOProcesser.MinIOProcessor;
import com.example.doc_intel.Repository.DocumentsRepository;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<DocumentResponseDTO> processDocuments(@NonNull UserEntity user, @NonNull List<MultipartFile> files,
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
            throw new UnSupportedFileException("File Type not support");
        }

        Optional<UserEntity> userOptional = userRepository.findByEmailAndIsActiveTrue(email);
        if (userOptional.isEmpty()) {
            throw new UserNotExistException("User not Exist");
        }

        Optional<DocumentEntity> documentOptional = documentsRepository.findByDocumentIdAndIsActiveTrue(documentId);
        if (documentOptional.isEmpty()) {
            throw new DocumentNotExistException("Document Id: %s not exist".formatted(documentId));
        }

        UserEntity user = userOptional.get();
        DocumentEntity document = documentOptional.get();

        String fileName = file.getOriginalFilename() == null
            ? "Document.%s".formatted(fileExtension) : file.getOriginalFilename();
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();

        ObjectWriteResponse writeResponse = minIOProcessor.putObject(file, document.getObjectKey());

        document.setFileName(fileName);
        document.setFileSize(file.getSize());
        document.setContentType(contentType);
        document.setUpdateAt(LocalDateTime.now());
        document.setUpdatedBy(email);
        document.setStatus(DocumentStatus.UPLOADED);
        document.setVersion(document.getVersion() + 1);
        document.setChunks(0);
        document.setFileExtensions(fileExtension);
        document.setMinIOVersionId(writeResponse.versionId());

        kafkaEventDTOS.add(createEvent(user, document, fileExtension, writeResponse.versionId()));
        return toResponse(document, file.getOriginalFilename());
    }

    private DocumentResponseDTO processOneDocument(UserEntity user, MultipartFile file,
                                                   List<KafkaEventDTO> events) {
        FileExtensions fileExtension = Utils.getExtension(file);

        if (fileExtension == null) {
            throw new UnSupportedFileException("File Type not support");
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
            .fileName(fileName)
            .objectKey(objectKey)
            .bucketName(Constants.MINIO_BUCKET_NAME)
            .contentType(contentType)
            .fileSize(file.getSize())
            .fileExtensions(fileExtension)
            .isActive(true)
            .version(1)
            .minIOVersionId(writeResponse.versionId())
            .status(DocumentStatus.UPLOADED)
            .user(user)
            .createdAt(now)
            .createdBy(user.getUsername())
            .updateAt(now)
            .updatedBy(user.getUsername())
            .chunks(0)
            .build();

        DocumentEntity savedDocument = documentsRepository.save(document);
        events.add(createEvent(user, savedDocument, fileExtension, writeResponse.versionId()));
        return toResponse(savedDocument, file.getOriginalFilename());
    }

    private KafkaEventDTO createEvent(UserEntity user, DocumentEntity document, FileExtensions extension,
                                      String minioVersion) {
        return KafkaEventDTO.builder()
            .eventId(UUID.randomUUID())
            .documentId(document.getDocumentId())
            .userId(user.getUserId())
            .objectKey(document.getObjectKey())
            .fileName(document.getFileName())
            .fileExtensions(extension)
            .documentVersion(document.getVersion())
            .minIOVersion(minioVersion)
            .build();
    }

    private DocumentResponseDTO toResponse(DocumentEntity document, String originalFileName) {
        return DocumentResponseDTO.builder()
            .version(document.getVersion())
            .URI(null)
            .documentId(document.getDocumentId())
            .fileName(originalFileName)
            .fileSize(document.getFileSize())
            .fileExtensions(document.getFileExtensions())
            .chunks(document.getChunks())
            .createdAt(document.getCreatedAt())
            .updatedAt(document.getUpdateAt())
            .status(document.getStatus())
            .build();
    }
}
