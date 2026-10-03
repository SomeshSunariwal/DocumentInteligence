package com.example.doc_intel.Exceptions;

public class UnAuthenticatedUser extends CodedRuntimeException {

    public UnAuthenticatedUser(String message, int errorCode) {
        super(message, errorCode);
    }
}
