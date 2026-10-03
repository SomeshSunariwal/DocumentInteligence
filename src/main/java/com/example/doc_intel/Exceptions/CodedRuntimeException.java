package com.example.doc_intel.Exceptions;

import lombok.Getter;

@Getter
public abstract class CodedRuntimeException extends RuntimeException {

    private final int errorCode;

    protected CodedRuntimeException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected CodedRuntimeException(String message, int errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
