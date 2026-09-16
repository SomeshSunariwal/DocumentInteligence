package com.example.doc_intel.Exceptions.DBExceptions;

public class DocumentNotExistException extends RuntimeException {
    public DocumentNotExistException(String message) {
        super(message);
    }
}
