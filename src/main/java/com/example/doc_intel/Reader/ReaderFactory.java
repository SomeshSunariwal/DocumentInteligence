package com.example.doc_intel.Reader;

import org.springframework.stereotype.Component;

@Component
public class ReaderFactory {

   private final TextFileReader textFileReader;
   private final PDFReader pdfReader;

    public ReaderFactory(TextFileReader textFileReader, PDFReader pdfReader) {
        this.textFileReader = textFileReader;
        this.pdfReader = pdfReader;
    }

    public Reader getDataFromReader(String type) {
        if (type.equals("pdf")) {
            return pdfReader;
        } return textFileReader;
    }
}
