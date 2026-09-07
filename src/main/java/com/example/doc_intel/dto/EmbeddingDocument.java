package com.example.doc_intel.dto;

import dev.langchain4j.data.document.Metadata;

import java.util.List;

public class EmbeddingDocument {
    private List<Float> vector;
    private String text;
    private OpenSearchMetaDataDTO metadata;

    public EmbeddingDocument() {
    }

    public EmbeddingDocument(List<Float> vector, String text, OpenSearchMetaDataDTO metadata) {
        this.vector = vector;
        this.text = text;
        this.metadata = metadata;
    }

    public List<Float> getVector() {
        return vector;
    }

    public void setVector(List<Float> vector) {
        this.vector = vector;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public OpenSearchMetaDataDTO getMetadata() {
        return metadata;
    }

    public void setMetadata(OpenSearchMetaDataDTO metadata) {
        this.metadata = metadata;
    }
}
