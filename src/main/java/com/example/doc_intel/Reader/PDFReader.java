package com.example.doc_intel.Reader;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class PDFReader implements Reader {
    private final Tika tika = new Tika();

    @Override
    public String getParseFileData(final MultipartFile file) throws Exception {
        try {
            // 1. Read PDF
            byte[] pdfBytes = file.getBytes();
            System.out.println("Received file: " + file.getOriginalFilename());

            // 2. Extract text using Apache Tika
            String text = tika.parseToString(new java.io.ByteArrayInputStream(pdfBytes));
            System.out.println("Extracted characters: " + text.length());

            return text;
        } catch (IOException | TikaException exception) {
            throw new Exception("IO Exception" + exception.getMessage());
        } catch (Exception e) {
            throw new Exception("Exception" + e.getMessage());
        }
    }
}
