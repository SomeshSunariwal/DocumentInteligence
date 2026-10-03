package com.example.doc_intel.Exceptions.ChatModelExceptions;

import com.example.doc_intel.Exceptions.CodedRuntimeException;

public class NoResultFoundException extends CodedRuntimeException {

    public NoResultFoundException(String message, int errorCode) {
        super(message, errorCode);
    }
}
