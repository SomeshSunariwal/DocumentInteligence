package com.example.doc_intel.Exceptions;

public class FileReadError extends CodedRuntimeException {

    public FileReadError(String message, int errorCode) {
        super(message, errorCode);
    }
}
