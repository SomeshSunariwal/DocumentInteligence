package com.example.doc_intel.Exceptions.EncoderException;

import com.example.doc_intel.Exceptions.CodedRuntimeException;

public class WordDocumentEncoderException extends CodedRuntimeException {

    public WordDocumentEncoderException(String message, int errorCode) {
        super(message, errorCode);
    }
}
