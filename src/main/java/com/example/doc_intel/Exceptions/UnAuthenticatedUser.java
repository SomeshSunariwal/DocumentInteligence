package com.example.doc_intel.Exceptions;

public class UnAuthenticatedUser extends RuntimeException {
    public UnAuthenticatedUser(String message) {
        super(message);
    }
}
