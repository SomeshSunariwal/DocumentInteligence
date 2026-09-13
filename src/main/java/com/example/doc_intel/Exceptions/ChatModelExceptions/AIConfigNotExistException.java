package com.example.doc_intel.Exceptions.ChatModelExceptions;

public class AIConfigNotExistException extends  RuntimeException {
    public AIConfigNotExistException(String message) {
        super(message);
    }
}
