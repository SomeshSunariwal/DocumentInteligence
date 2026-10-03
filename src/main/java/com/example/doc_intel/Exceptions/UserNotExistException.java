package com.example.doc_intel.Exceptions;

public class UserNotExistException extends CodedRuntimeException {
    public UserNotExistException(String message, int errorCode) {
        super(message, errorCode);
    }
}
