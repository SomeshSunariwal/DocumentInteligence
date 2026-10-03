package com.example.doc_intel.Exceptions;

public class InternalServerErrorException extends CodedRuntimeException {

    public InternalServerErrorException(String message, int errorCode) {
        super(message, errorCode);
    }

    public InternalServerErrorException(String message, int errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
