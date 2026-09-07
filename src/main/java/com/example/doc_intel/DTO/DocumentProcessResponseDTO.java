package com.example.doc_intel.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentProcessResponseDTO {
    String message;
    Integer Chunksize;
}
