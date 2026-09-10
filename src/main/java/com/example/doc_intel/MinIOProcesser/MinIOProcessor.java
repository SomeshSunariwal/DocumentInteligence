package com.example.doc_intel.MinIOProcesser;

import io.minio.GetObjectResponse;
import io.minio.ObjectWriteResponse;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface MinIOProcessor {

    ObjectWriteResponse putObject(@NonNull String userName, @NonNull MultipartFile file);

    GetObjectResponse getObject(@NonNull String username, @NonNull UUID uuid, @NonNull String fileName);

}
