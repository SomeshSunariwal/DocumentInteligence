package com.example.doc_intel.MinIOProcesser.MinIOProcessorImpl;

import com.example.doc_intel.Client.MinIOClientProvider;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Exceptions.InternalServerErrorException;
import com.example.doc_intel.Exceptions.MinIOExceptions.MinIOObjectPutException;
import com.example.doc_intel.MinIOProcesser.MinIOProcessor;
import io.minio.PutObjectArgs;
import io.minio.ObjectWriteResponse;
import io.minio.GetObjectResponse;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinIOProcessorImpl implements MinIOProcessor {

    private final MinIOClientProvider minIOClientProvider;

    public ObjectWriteResponse putObject(@NonNull MultipartFile file, @NonNull String objectKey) {
        try {
            return minIOClientProvider.getClient().putObject(
                    PutObjectArgs
                            .builder()
                            .object(objectKey)
                            .bucket(Constants.MINIO_BUCKET_NAME)
                            .stream(file.getInputStream(), file.getSize(), -1L) // Known Size Object
                            .contentType(file.getContentType())
                            .maxRetries(3)
                            .build());
        } catch (Exception e) {
            throw new MinIOObjectPutException("Exception: {}" + e.getMessage());
        }
    }

    @Override
    public GetObjectResponse getObject(@NonNull String objectKey) {

        try {
            return minIOClientProvider.getClient().getObject(
                    GetObjectArgs.builder()
                            .bucket(Constants.MINIO_BUCKET_NAME)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.info("Exception During Getting Object: {}", e.getMessage());
            throw new InternalServerErrorException("Internal Server Exception");
        }
    }

    @Override
    public String getPresignedObjectUrl(@NonNull String objectKey) {

        try {
            return minIOClientProvider.getClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .object(objectKey)
                            .bucket(Constants.MINIO_BUCKET_NAME)
                            .expiry(60, TimeUnit.SECONDS)
                            .method(Http.Method.GET)
                            .build()
            );
        } catch (Exception e) {
            log.info("Exception During Getting Presigned Object URL: {}", e.getMessage());
            throw new InternalServerErrorException("Internal Server Exception");
        }
    }
}
