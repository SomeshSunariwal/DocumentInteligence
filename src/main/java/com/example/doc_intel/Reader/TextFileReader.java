package com.example.doc_intel.Reader;

import com.example.doc_intel.Exceptions.FileReadError;
import com.example.doc_intel.Exceptions.FileSupportError;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Component
public class TextFileReader implements Reader {
    @Override
    public String getParseFileData(MultipartFile file) {
        String contentType = file.getContentType();

        if (!"text/plain".equalsIgnoreCase(contentType)) {
            throw new FileSupportError("Header Content Type Should be 'text/plain'");
        }

        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new FileReadError("Error While Reading File");
        }
    }
}
