package com.example.doc_intel.MinIOProcesser.MinIOProcessorImpl;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.doc_intel.Client.MinIOClientProvider;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.Exceptions.InternalServerErrorException;
import com.example.doc_intel.Exceptions.MinIOExceptions.MinIOObjectPutException;
import com.example.doc_intel.MinIOProcesser.MinIOProcessor;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
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
            log.error("Failed to upload object to MinIO. objectKey={}", objectKey, e);
            throw new MinIOObjectPutException("Unable to upload object", ErrorCode.MinioObjectUploadFailed, e);
        }
    }

    @Override
    public GetObjectResponse getObject(@NonNull String objectKey, @Nullable String versionId) {
        try {
            return minIOClientProvider.getClient().getObject(
                GetObjectArgs.builder()
                    .bucket(Constants.MINIO_BUCKET_NAME)
                    .object(objectKey)
                    .versionId(versionId)
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to retrieve object from MinIO. objectKey={}", objectKey, e);
            throw new InternalServerErrorException("Unable to retrieve object", ErrorCode.MinioObjectReadFailed, e);
        }
    }

    @Override
    public String getPresignedObjectUrl(@NonNull String objectKey,
                                        @NonNull String contentType,
                                        @NonNull String fileName,
                                        @NonNull String minIOVersion) {
        Map<String, String> headers = Map.of(
            "response-content-type", "%s".formatted(contentType),
            "response-content-disposition", "inline; filename=%s".formatted(fileName)
        );

        try {
            return minIOClientProvider.getClient().getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .object(objectKey)
                    .bucket(Constants.MINIO_BUCKET_NAME)
                    .expiry(10, TimeUnit.MINUTES)
                    .extraQueryParams(headers)
                    .method(Http.Method.GET)
                    .versionId(minIOVersion)
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate MinIO presigned URL. objectKey={}", objectKey, e);
            throw new InternalServerErrorException("Unable to generate document URL", ErrorCode.MinioPresignedUrlFailed, e);
        }
    }
}
