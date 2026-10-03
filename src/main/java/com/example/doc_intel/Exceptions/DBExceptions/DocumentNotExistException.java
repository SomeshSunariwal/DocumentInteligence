package com.example.doc_intel.Exceptions.DBExceptions;

import com.example.doc_intel.Exceptions.CodedRuntimeException;

public class DocumentNotExistException extends CodedRuntimeException {

    public DocumentNotExistException(String message, int errorCode) {
        super(message, errorCode);
    }
}
