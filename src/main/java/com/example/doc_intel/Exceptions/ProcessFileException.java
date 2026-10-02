package com.example.doc_intel.Exceptions;

public class ProcessFileException extends RuntimeException {
    public ProcessFileException(String message) {
        super(message);
    }

    public ProcessFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
