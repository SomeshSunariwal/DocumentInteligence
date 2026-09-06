package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Exceptions.FileReadError;
import com.example.doc_intel.Exceptions.FileSupportError;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Component
public class TextFileDocumentEncoder implements DocumentEncoder {
    DocumentParser parser = new ApacheTikaDocumentParser();

    @Override
    public Document encode(MultipartFile file) {
        try {;
            return parser.parse(file.getInputStream());
        } catch (FileSupportError e) {
            throw e;
        } catch (Exception e) {
            throw new FileReadError("Error While Reading File");
        }
    }
}
