package com.example.doc_intel.Service;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.DocumentsDTO.DocumentResponseDTO;
import com.example.doc_intel.DTO.UserDTOs.UserDocumentsResponseDTO;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Exceptions.DocumentNotExistException;
import com.example.doc_intel.Exceptions.UnSupportedFileException;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.MinIOProcesser.MinIOProcessor;
import com.example.doc_intel.Repository.DocumentsRepository;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Utils.Utils;
import io.minio.ObjectWriteResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentService {

    private final MinIOProcessor minIOProcessor;

    private final UserRepository userRepository;

    private final DocumentsRepository documentsRepository;

    @Transactional
    public DocumentResponseDTO uploadFile(@NonNull String email,
                                          @NonNull MultipartFile file) {

        String fileExtension = Utils.getExtension(file);
        if (Objects.isNull(fileExtension)) {
            throw new UnSupportedFileException("File Type not support");
        }
        // User Check
        UserEntity userEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (Objects.isNull(userEntity)) {
            log.info("User Not Exist");
            throw new UserNotExistException("User not Exist");
        }

        String userName = userEntity.getUsername();

        // Placed Document Object
        UUID uuid = UUID.randomUUID();
        String objectKey = Utils.getObjectKey(userName, uuid, Objects.requireNonNull(file.getOriginalFilename()));
        DocumentEntity documentEntity = DocumentEntity.builder()
                .documentId(uuid)
                .fileName(Objects.requireNonNull(file.getOriginalFilename()))
                .objectKey(objectKey)
                .bucketName(Constants.MINIO_BUCKET_NAME)
                .contentType(Objects.requireNonNull(file.getContentType()))
                .fileSize(file.getSize())
                .createdAt(LocalDateTime.now())
                .createdBy(userName)
                .updateAt(LocalDateTime.now())
                .updatedBy(userName)
                .isActive(true)
                .version(1)
                .user(userEntity)
                .build();

        DocumentEntity documentEntityResponse = documentsRepository.save(documentEntity);

        // Upload File to MinIO
        ObjectWriteResponse objectWriteResponse = minIOProcessor.putObject(file, objectKey);
        log.info("Version Created with : {}", objectWriteResponse.versionId());

        return DocumentResponseDTO.builder()
                .version(documentEntityResponse.getVersion())
                .URI(null)
                .documentId(documentEntityResponse.getDocumentId())
                .fileName(file.getOriginalFilename())
                .createdAt(documentEntity.getCreatedAt())
                .updatedAt(documentEntity.getUpdateAt())
                .build();
    }

    @Transactional
    public DocumentResponseDTO updateDocument(@NonNull String email,
                                              @NonNull UUID documentId,
                                              @NonNull MultipartFile file) {

        String fileExtension = Utils.getExtension(file);
        if (Objects.isNull(fileExtension)) {
            throw new UnSupportedFileException("File Type not support");
        }

        // User Check
        UserEntity userEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (Objects.isNull(userEntity)) {
            log.info("User Email: {} not exist", email);
            throw new UserNotExistException("User not Exist");
        }

        // Documen Check
        DocumentEntity documentEntity = documentsRepository.findByDocumentIdAndIsActiveTrue(documentId);
        if (Objects.isNull(documentEntity)) {
            log.info("Document Id: {} not exist", documentId);
            throw new DocumentNotExistException("Document not Exist");
        }

        // Updated Document in the Table
        String objectKey = documentEntity.getObjectKey();
        documentEntity.setFileName(Objects.requireNonNull(file.getOriginalFilename()));
        documentEntity.setFileSize(file.getSize());
        documentEntity.setContentType(Objects.requireNonNull(file.getContentType()));
        documentEntity.setUpdateAt(LocalDateTime.now());
        documentEntity.setUpdatedBy(email);
        documentEntity.setVersion(documentEntity.getVersion() + 1);

        // Upload File to MinIO
        ObjectWriteResponse objectWriteResponse = minIOProcessor.putObject(file, objectKey);
        log.info("Version Updated to : {}", objectWriteResponse.versionId());

        return DocumentResponseDTO.builder()
                .version(documentEntity.getVersion())
                .URI(null)
                .documentId(documentEntity.getDocumentId())
                .fileName(file.getOriginalFilename())
                .createdAt(documentEntity.getCreatedAt())
                .updatedAt(documentEntity.getUpdateAt())
                .build();
    }

    @Transactional
    public DocumentResponseDTO deleteDocument(@NonNull String email,
                                              @NonNull UUID documentId) {

        // User Check
        UserEntity userEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (Objects.isNull(userEntity)) {
            log.info("User Email: {} not exist", email);
            throw new UserNotExistException("User not Exist");
        }

        // Documen Check
        DocumentEntity documentEntity = documentsRepository.findByDocumentIdAndIsActiveTrue(documentId);
        if (Objects.isNull(documentEntity)) {
            log.info("Document Id: {} not exist", documentId);
            throw new DocumentNotExistException("Document not Exist");
        }
        // Soft Deleting Document in the Table
        documentEntity.setIsActive(false);

        return DocumentResponseDTO.builder()
                .version(documentEntity.getVersion())
                .URI(null)
                .documentId(documentEntity.getDocumentId())
                .fileName(documentEntity.getFileName())
                .createdAt(documentEntity.getCreatedAt())
                .updatedAt(documentEntity.getUpdateAt())
                .build();
    }

    @Transactional
    public UserDocumentsResponseDTO getUserAllDocuments(@NonNull String email) {
        // User Check
        UserEntity userEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (Objects.isNull(userEntity)) {
            log.info("User Email: {} not exist", email);
            throw new UserNotExistException("User not Exist");
        }

        // Documen Check
        List<DocumentEntity> documentEntity = documentsRepository.findByUser_UserIdAndIsActiveTrue(userEntity.getUserId());
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
                            .build();
                }).toList();

        return UserDocumentsResponseDTO.builder()
                .userId(userEntity.getUserId())
                .username(userEntity.getUsername())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .email(userEntity.getEmail())
                .documents(documents)
                .build();
    }
}
