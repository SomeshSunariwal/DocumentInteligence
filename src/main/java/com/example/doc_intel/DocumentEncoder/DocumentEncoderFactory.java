package com.example.doc_intel.DocumentEncoder;

import org.springframework.stereotype.Component;

@Component
public class DocumentEncoderFactory {

    private final TextFileDocumentEncoder textFileDocumentEncoder;

    private final PDFDocumentEncoder pdfDocumentEncoder;

    public DocumentEncoderFactory(TextFileDocumentEncoder textFileDocumentParser, PDFDocumentEncoder pdfDocumentParser) {
        this.textFileDocumentEncoder = textFileDocumentParser;
        this.pdfDocumentEncoder = pdfDocumentParser;
    }

    public DocumentEncoder getParser(String type) {
        if (type.equals("pdf")) {
            return pdfDocumentEncoder;
        } return textFileDocumentEncoder;
    }
}
