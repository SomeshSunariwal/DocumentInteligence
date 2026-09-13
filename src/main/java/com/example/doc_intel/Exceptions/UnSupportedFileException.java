package com.example.doc_intel.Exceptions;

public class UnSupportedFileException extends RuntimeException {
    public UnSupportedFileException(String message) {
        super(message);
    }
}
