package com.example.doc_intel.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextSegmentResponseDTO {

    private String fileName;

    private Integer pageNumber;

    private Integer lineNumber;

    private String score;

    private String text;

    private Integer version;

    private String userId;
}
