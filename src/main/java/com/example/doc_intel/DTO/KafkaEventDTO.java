package com.example.doc_intel.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KafkaEventDTO {

    @NonNull
    private UUID eventId;

    @NonNull
    private UUID userId;

    @NonNull
    private UUID documentId;

    @NonNull
    private String objectKey;

    @NonNull
    private String fileName;

    @NonNull
    private Integer version;

    //MinIO versionId
    @NonNull
    private String versionId;

}
