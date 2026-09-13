package com.example.doc_intel.MinIOProcesser;

import io.minio.GetObjectResponse;
import io.minio.ObjectWriteResponse;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

public interface MinIOProcessor {

    ObjectWriteResponse putObject(@NonNull MultipartFile file, @NonNull String objectKey);

    GetObjectResponse getObject(@NonNull String objectKey);

    String getPresignedObjectUrl(@NonNull String objectKey);

}
