package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Enums.FileExtensions;
import org.springframework.stereotype.Component;

@Component
public class DocumentEncoderFactory {

    private final TextFileDocumentEncoder textFileDocumentEncoder;

    private final PDFDocumentEncoder pdfDocumentEncoder;

    public DocumentEncoderFactory(TextFileDocumentEncoder textFileDocumentParser,
                                  PDFDocumentEncoder pdfDocumentParser) {
        this.textFileDocumentEncoder = textFileDocumentParser;
        this.pdfDocumentEncoder = pdfDocumentParser;
    }

    public DocumentEncoder getParser(FileExtensions extensions) {
        if (FileExtensions.PDF.equals(extensions)) {
            return pdfDocumentEncoder;
        }
        return textFileDocumentEncoder;
    }
}
