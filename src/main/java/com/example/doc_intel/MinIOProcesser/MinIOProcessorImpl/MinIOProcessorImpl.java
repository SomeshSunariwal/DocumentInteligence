package com.example.doc_intel.MinIOProcesser.MinIOProcessorImpl;

import com.example.doc_intel.Client.MinIOClientProvider;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Exceptions.InternalServerErrorException;
import com.example.doc_intel.Exceptions.MinIOObjectPutException;
import com.example.doc_intel.MinIOProcesser.MinIOProcessor;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinIOProcessorImpl implements MinIOProcessor {

    private final MinIOClientProvider minIOClientProvider;

    public ObjectWriteResponse putObject(@NonNull String userName, @NonNull MultipartFile file) {
        try {
            UUID uuid = UUID.randomUUID();

            String objectKey = userName + File.separator + uuid + File.separator + file.getOriginalFilename();

            return minIOClientProvider.getClient().putObject(
                    PutObjectArgs
                            .builder()
                            .object(objectKey)
                            .bucket(Constants.BUCKET_NAME)
                            .stream(file.getInputStream(), file.getSize(), -1L) // Known Size Object
                            .contentType(file.getContentType())
                            .maxRetries(3)
                            .build());
        } catch (Exception e) {
            throw new MinIOObjectPutException("Exception: {}" + e.getMessage());
        }
    }

    @Override
    public GetObjectResponse getObject(@NonNull String userName, @NonNull UUID documentUUID, @NonNull String fileName) {

        String objectKey = userName + File.separator + documentUUID + File.separator + fileName;

        try {
            return minIOClientProvider.getClient().getObject(
                    GetObjectArgs.builder()
                            .bucket(Constants.BUCKET_NAME)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.info("Exception During Getting Object: {}", e.getMessage());
            throw new InternalServerErrorException("Internal Server Exception");
        }
    }
}
