package com.example.doc_intel.Exceptions.MinIOExceptions;

import com.example.doc_intel.Exceptions.CodedRuntimeException;

public class MinIOObjectPutException extends CodedRuntimeException {

    public MinIOObjectPutException(String message, int errorCode) {
        super(message, errorCode);
    }

    public MinIOObjectPutException(String message, int errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
