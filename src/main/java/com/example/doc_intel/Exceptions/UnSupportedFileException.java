package com.example.doc_intel.Exceptions;

public class UnSupportedFileException extends CodedRuntimeException {

    public UnSupportedFileException(String message, int errorCode) {
        super(message, errorCode);
    }
}
