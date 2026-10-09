package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.DTO.EncoderModel;
import com.example.doc_intel.Utils.EncoderUtils;
import com.example.doc_intel.Exceptions.FileReadError;
import com.example.doc_intel.Constants.ErrorCode;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
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

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    private final static Integer MAX_LINES_PER_PAGE = 25;

    private final static Integer MAX_CHARS_PER_PAGE = 78;

    @Override
    public List<TextSegment> encode(@NonNull final EncoderModel encoderModel) {
        log.info("Using Text File Encoder");

        try {
            List<TextSegment> segments = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(encoderModel.getFileStream(), StandardCharsets.UTF_8))) {
                String fileName = encoderModel.getFileName();
                List<String> pageLines = new ArrayList<>();
                String line;
                int pageNumber = 1;
                Integer chunk = 1;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    String normalizedLine = EncoderUtils.normalizeText(line);
                    if (normalizedLine.isBlank()) {
                        continue;
                    }
                    // Split long physical lines into max 78-character lines
                    int start = 0;
                    while (start < normalizedLine.length()) {
                        int maxEnd = Math.min(start + MAX_CHARS_PER_PAGE, normalizedLine.length());
                        int end = maxEnd;

                        // If we haven't reached the end of the line,
                        // try to break at the last whitespace.
                        if (maxEnd < normalizedLine.length()) {
                            int lastSpace = normalizedLine.lastIndexOf(' ', maxEnd);
                            if (lastSpace > start) {
                                end = lastSpace;
                            }
                        }

                        String virtualLine = normalizedLine.substring(start, end).trim();
                        if (!virtualLine.isBlank()) {
                            pageLines.add(virtualLine);
                        }
                        // 25 virtual lines = one page
                        if (pageLines.size() == MAX_LINES_PER_PAGE) {
                            chunk = EncoderUtils.addPageChunks(segments, pageLines, fileName, pageNumber, encoderModel, chunk, embeddingModel);
                            pageLines.clear();
                            pageNumber++;
                        }
                        // Move forward
                        start = end;
                        // Skip the whitespace where we broke the line
                        while (start < normalizedLine.length()
                            && Character.isWhitespace(normalizedLine.charAt(start))) {
                            start++;
                        }
                    }
                }
                // Process remaining lines (< 25)
                if (!pageLines.isEmpty()) {
                    EncoderUtils.addPageChunks(segments, pageLines, fileName, pageNumber, encoderModel, chunk, embeddingModel);
                }
                return segments;
            }

        } catch (Exception e) {
            log.error("Error while reading text file: {}", encoderModel.getFileName(), e);
            throw new FileReadError("Error While Reading File", ErrorCode.TextFileReadFailed);
        }
    }

}
