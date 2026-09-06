package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Exceptions.FileReadError;
import com.example.doc_intel.Exceptions.InternalServerErrorException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class PDFDocumentEncoder implements DocumentEncoder {

    DocumentParser parser = new ApacheTikaDocumentParser();

    @Override
    public Document encode(final MultipartFile file)  {
        try {
            // 2. Extract text using Apache Tika
            Document document = parser.parse(file.getInputStream());
            System.out.println("Extracted characters: " + document.text());

            return document;
        } catch (IOException exception) {
            throw new FileReadError("Error While Reading PDF File");
        } catch (Exception e) {
            throw new InternalServerErrorException("Internal Server Error");
        }
    }
}
