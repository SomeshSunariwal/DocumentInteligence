package com.example.doc_intel.Exceptions;

public class PSQLDBException extends CodedRuntimeException {

    public PSQLDBException(String message, int errorCode) {
        super(message, errorCode);
    }
}
