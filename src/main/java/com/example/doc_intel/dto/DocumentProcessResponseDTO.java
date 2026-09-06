package com.example.doc_intel.dto;

public class DocumentProcessResponseDTO {
    String message;
    Integer Chunksize;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getChunksize() {
        return Chunksize;
    }

    public DocumentProcessResponseDTO(String message, Integer chunksize) {
        this.message = message;
        Chunksize = chunksize;
    }

    public void setChunksize(Integer chunksize) {
        Chunksize = chunksize;
    }
}
