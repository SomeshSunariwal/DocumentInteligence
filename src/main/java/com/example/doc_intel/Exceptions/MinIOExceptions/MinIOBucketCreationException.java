package com.example.doc_intel.Exceptions.MinIOExceptions;

import com.example.doc_intel.Exceptions.CodedRuntimeException;

public class MinIOBucketCreationException extends CodedRuntimeException {

    public MinIOBucketCreationException(String message, int errorCode) {
        super(message, errorCode);
    }
}
