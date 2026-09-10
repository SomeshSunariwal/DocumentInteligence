package com.example.doc_intel.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDTO {

    @NonNull
    String message;

    @NonNull
    String email;

    @NonNull
    String fileName;

    @NonNull
    UUID documentId;

    @NonNull
    String version;

}
