package com.example.doc_intel.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddingDocument {

    private List<Float> vector;

    private String text;

    private OpenSearchMetaDataDTO metadata;

}
