package com.example.doc_intel.DTO.DocumentsDTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDTO {

    @NonNull
    private String fileName;

    @NonNull
    private UUID documentId;

    private String URI;

    @NonNull
    private Integer version;

    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private LocalDateTime updatedAt;
}
