package com.example.doc_intel.DTO.DocumentsDTO;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetDocumentResponseDTO {

    @NonNull
    private UUID documentId;

    @Nullable
    private List<DocumentResponseDTO> documentVersions = new ArrayList<>();

}
