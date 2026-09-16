package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.EncoderModel;
import com.example.doc_intel.Exceptions.FileReadError;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TextFileDocumentEncoder implements DocumentEncoder {

    @Override
    public List<TextSegment> encode(@NonNull final EncoderModel encoderModel) {
        log.info("Using Text File Encoder");
        try {
            List<TextSegment> segments = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    encoderModel.getFileStream(),
                    StandardCharsets.UTF_8))) {

                String fileName = encoderModel.getFileName();
                String line;
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        lineNumber++;
                        continue;
                    }
                    Metadata metadata = new Metadata();
                    metadata.put(Constants.META_DATA_FILE_NAME, fileName);
                    metadata.put(Constants.META_DATA_LINE_NUMBER, lineNumber);
                    metadata.put(Constants.META_DATA_PAGE_NUMBER, 0);
                    metadata.put(Constants.META_USER_ID, encoderModel.getUserId());
                    metadata.put(Constants.META_DOCUMENT_VERSION, encoderModel.getVersion());
                    metadata.put(Constants.META_DOCUMENT_ID, encoderModel.getDocumentId());
                    TextSegment segment =
                        TextSegment.from(line, metadata);

                    segments.add(segment);
                    lineNumber++;
                }
                return segments;
            }
        } catch (Exception e) {
            throw new FileReadError("Error While Reading File");
        }
    }
}
