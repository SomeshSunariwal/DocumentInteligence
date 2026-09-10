package com.example.doc_intel.Service;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.DocumentResponseDTO;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Exceptions.InternalServerErrorException;
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
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileHandlingService {

    private final MinIOProcessor minIOProcessor;

    private final UserRepository userRepository;

    private final DocumentsRepository documentsRepository;

    @Transactional
    public DocumentResponseDTO uploadFile(@NonNull String userName,
                                            @NonNull MultipartFile file) {

        // User Check
        UserEntity userEntity = userRepository.findByUsername(userName);
        if (Objects.isNull(userEntity)) {
            log.info("User Not Exist");
            throw new InternalServerErrorException("User not Exist");
        }

        // Placed Document Object
        UUID uuid = UUID.fromString(file.getName());
        String objectKey = Utils.getObjectKey(userName, uuid, file.getName());
        DocumentEntity documentEntity = DocumentEntity.builder()
                .documentId(uuid)
                .fileName(file.getName())
                .objectKey(objectKey)
                .bucket_name(Constants.BUCKET_NAME)
                .content_type(Objects.requireNonNull(file.getContentType()))
                .createdAt(LocalDateTime.now())
                .createdBy(userName)
                .updateAt(LocalDateTime.now())
                .updatedBy(userName)
                .isActive(true)
                .user(userEntity)
                .build();

        DocumentEntity documentEntityResponse = documentsRepository.save(documentEntity);

        // Upload File to MinIO
        ObjectWriteResponse objectWriteResponse = minIOProcessor.putObject(userName, file);

        return DocumentResponseDTO.builder()
                .message("Successfully Uploaded")
                .version(objectWriteResponse.versionId())
                .email(documentEntityResponse.getUser().getEmail())
                .documentId(uuid)
                .build();
    }
}
