package com.example.doc_intel.Exceptions.ChatModelExceptions;

import com.example.doc_intel.Exceptions.CodedRuntimeException;

public class AIConfigNotExistException extends CodedRuntimeException {

    public AIConfigNotExistException(String message, int errorCode) {
        super(message, errorCode);
    }
}
