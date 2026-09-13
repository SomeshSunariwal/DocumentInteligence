package com.example.doc_intel.Exceptions;

public class DocumentNotExistException extends RuntimeException {
    public DocumentNotExistException(String message) {
        super(message);
    }
}
