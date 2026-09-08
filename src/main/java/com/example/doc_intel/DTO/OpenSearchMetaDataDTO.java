package com.example.doc_intel.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenSearchMetaDataDTO {

    private String fileName;

    private Integer pageNumber;

    private Integer lineNumber;

    private String text;
}
