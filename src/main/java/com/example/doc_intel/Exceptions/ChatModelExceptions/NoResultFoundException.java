package com.example.doc_intel.Exceptions.ChatModelExceptions;

public class NoResultFoundException extends RuntimeException {
    public NoResultFoundException(String message) {
        super(message);
    }
}
