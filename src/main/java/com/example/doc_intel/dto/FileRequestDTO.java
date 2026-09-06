package com.example.doc_intel.dto;

import org.springframework.stereotype.Component;

public class FileRequestDTO {
    String[] message;

    public String[] getMessage() {
        return message;
    }

    public void setMessage(String[] message) {
        this.message = message;
    }
}
