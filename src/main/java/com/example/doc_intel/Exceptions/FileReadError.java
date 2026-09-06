package com.example.doc_intel.Exceptions;

public class FileReadError extends RuntimeException {
    public FileReadError(String message) {
        super(message);
    }
}
