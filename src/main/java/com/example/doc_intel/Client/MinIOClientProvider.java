package com.example.doc_intel.Client;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Exceptions.BuckerCreationException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MinIOClientProvider implements Client<MinioClient> {

    private final MinioClient client;

    public MinIOClientProvider(
            @Value(("${MINIO.HOST.URL}")) String ENDPOINT,
            @Value(("${MINIO.ROOT.USER}")) String ACCESS_KEY,
            @Value(("${MINIO.ROOT.PASSWORD}")) String SECRET
    ) {
        log.info("Endpoint: {}, Access: {}, Secret: {}", ENDPOINT, ACCESS_KEY, SECRET);
        this.client = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET)
                .build();
    }

    @Override
    public MinioClient getClient() {
        return client;
    }

    @PostConstruct
    void checkBucket() {
        try {
            boolean found = getClient()
                    .bucketExists(
                            BucketExistsArgs
                                    .builder()
                                    .bucket(Constants.BUCKET_NAME)
                                    .build());
            if (!found) {
                getClient().makeBucket(MakeBucketArgs.builder().bucket(Constants.BUCKET_NAME).build());
                log.info("Bucket: {} Created.", Constants.BUCKET_NAME);
            } else {
                log.info("Bucket: {} already exists.", Constants.BUCKET_NAME);
            }
        } catch (Exception e) {
            throw new BuckerCreationException("Exception: " + e.getMessage());
        }
    }
}
