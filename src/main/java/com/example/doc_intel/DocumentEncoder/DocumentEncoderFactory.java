package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Enums.FileExtensions;
import org.springframework.stereotype.Component;

@Component
public class DocumentEncoderFactory {

    private final TextFileDocumentEncoder textFileDocumentEncoder;

    private final PDFDocumentEncoder pdfDocumentEncoder;

    private final WordDocumentEncoder wordDocumentEncoder;

    public DocumentEncoderFactory(TextFileDocumentEncoder textFileDocumentParser,
                                  PDFDocumentEncoder pdfDocumentParser, WordDocumentEncoder wordDocumentEncoder) {
        this.textFileDocumentEncoder = textFileDocumentParser;
        this.pdfDocumentEncoder = pdfDocumentParser;
        this.wordDocumentEncoder = wordDocumentEncoder;
    }

    public DocumentEncoder getParser(FileExtensions extensions) {
        if (FileExtensions.PDF.equals(extensions)) {
            return pdfDocumentEncoder;
        }
        if (FileExtensions.DOC.equals(extensions) || FileExtensions.DOCX.equals(extensions)) {
            return wordDocumentEncoder;
        }
        return textFileDocumentEncoder;
    }
}
