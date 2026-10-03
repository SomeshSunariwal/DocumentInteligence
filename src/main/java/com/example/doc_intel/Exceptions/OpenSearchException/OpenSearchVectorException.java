package com.example.doc_intel.Exceptions.OpenSearchException;

import com.example.doc_intel.Exceptions.CodedRuntimeException;

public class OpenSearchVectorException extends CodedRuntimeException {

    public OpenSearchVectorException(String message, int errorCode) {
        super(message, errorCode);
    }
}
