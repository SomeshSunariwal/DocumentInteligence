package com.example.doc_intel.Exceptions.MinIOExceptions;

public class MinIOObjectPutException extends RuntimeException {
    public MinIOObjectPutException(String message) {
        super(message);
    }

    public MinIOObjectPutException(String message, Throwable cause) {
        super(message, cause);
    }
}
