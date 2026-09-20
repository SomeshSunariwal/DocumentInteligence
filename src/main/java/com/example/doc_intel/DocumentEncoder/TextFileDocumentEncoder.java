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

            final int chunkSize = encoderModel.getLine();
            final int overlap = encoderModel.getOverlapLine();
            final int step = chunkSize - overlap;
            final int maxLinesPerPage = 25;
            final int maxCharsPerLine = 78;

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
                    String normalizedLine = normalizeText(line);
                    if (normalizedLine.isBlank()) {
                        continue;
                    }
                    // Split long physical lines into max 78-character lines
                    int start = 0;
                    while (start < normalizedLine.length()) {
                        int maxEnd = Math.min(start + maxCharsPerLine, normalizedLine.length());
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
                        if (pageLines.size() == maxLinesPerPage) {
                            chunk = addChunks(segments, pageLines, pageNumber, fileName, encoderModel, chunkSize, step,
                                chunk);
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
                    addChunks(segments, pageLines, pageNumber, fileName, encoderModel, chunkSize, step, chunk);
                }
                return segments;
            }

        } catch (Exception e) {
            log.error("Error while reading text file: {}", encoderModel.getFileName(), e);
            throw new FileReadError("Error While Reading File");
        }
    }

    private Integer addChunks(List<TextSegment> segments, List<String> pageLines, int pageNumber, String fileName,
                              EncoderModel encoderModel, int chunkSize, int step, Integer chunkIndex) {

        for (int start = 0; start < pageLines.size(); start += step) {
            int end = Math.min(start + chunkSize, pageLines.size());
            String chunk = String.join("\n", pageLines.subList(start, end));
            if (chunk.isBlank()) {
                continue;
            }
            Metadata metadata = new Metadata();
            metadata.put(Constants.META_DATA_FILE_NAME, fileName);
            metadata.put(Constants.META_DATA_PAGE_NUMBER, pageNumber);
            metadata.put(Constants.META_DATA_LINE_NUMBER, start + 1);
            metadata.put(Constants.META_USER_ID, encoderModel.getUserId());
            metadata.put(Constants.META_DOCUMENT_VERSION, encoderModel.getDocumentVersion());
            metadata.put(Constants.META_DOCUMENT_ID, encoderModel.getDocumentId());
            metadata.put(Constants.META_CHUNK_INDEX, chunkIndex++);
            segments.add(TextSegment.from(chunk, metadata));
        }
        return chunkIndex;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace('\u00A0', ' ')
            .replace('\u2007', ' ')
            .replace('\u202F', ' ')
            .trim();
    }
}
