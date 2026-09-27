package com.example.doc_intel.DTO.DocumentsDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DocumentSummeryResponse {

    private String type;

    private String data;

    private Boolean success;

    private Boolean error;
}
