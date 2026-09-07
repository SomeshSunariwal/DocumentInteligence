package com.example.doc_intel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextSegmentResponseDTO {
    private String fileName;
    private Integer pageNumber;
    private Integer lineNumber;
    private String score;
    private String text;
}
