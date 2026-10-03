package com.example.doc_intel.Exceptions;

public class ProcessFileException extends CodedRuntimeException {

    public ProcessFileException(String message, int errorCode) {
        super(message, errorCode);
    }

    public ProcessFileException(String message, int errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
