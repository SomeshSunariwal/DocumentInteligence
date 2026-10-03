package com.example.doc_intel.Exceptions.OpenSearchException;

import com.example.doc_intel.Exceptions.CodedRuntimeException;

public class UnsupportedFilterException extends CodedRuntimeException {

    public UnsupportedFilterException(String message, int errorCode) {
        super(message, errorCode);
    }
}
