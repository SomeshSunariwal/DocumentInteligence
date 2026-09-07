package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Exceptions.FileReadError;
import com.example.doc_intel.Exceptions.FileSupportError;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TextFileDocumentEncoder implements DocumentEncoder {

    private static final Logger log = LoggerFactory.getLogger(TextFileDocumentEncoder.class);

    @Override
    public List<TextSegment> encode(MultipartFile file) {
        log.info("Using Text File Encoder");
        try {
            List<TextSegment> segments = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            file.getInputStream(),
                            StandardCharsets.UTF_8))) {

                String fileName = file.getOriginalFilename();
                String line;
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        lineNumber++;
                        continue;
                    }
                    Metadata metadata = new Metadata();
                    metadata.put(Constants.FILE_NAME, fileName);
                    metadata.put(Constants.LINE_NUMBER, lineNumber);
                    metadata.put(Constants.PAGE_NUMBER, 0);
                    metadata.put(Constants.TEXT, line);
                    TextSegment segment =
                            TextSegment.from(line, metadata);

                    segments.add(segment);
                    lineNumber++;
                }
                return segments;
            }
        } catch (FileSupportError e) {
            throw e;
        } catch (Exception e) {
            throw new FileReadError("Error While Reading File");
        }
    }
}
