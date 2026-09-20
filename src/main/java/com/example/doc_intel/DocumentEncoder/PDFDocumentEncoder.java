package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.EncoderModel;
import com.example.doc_intel.Exceptions.FileReadError;
import com.example.doc_intel.Exceptions.InternalServerErrorException;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PDFDocumentEncoder implements DocumentEncoder {

    @Override
    public List<TextSegment> encode(@NonNull final EncoderModel encoderModel) {
        log.info("Using PDF Encoder");

        try {
            List<TextSegment> segments = new ArrayList<>();
            String fileName = encoderModel.getFileName();
            try (PDDocument pdf = Loader.loadPDF(encoderModel.getFileStream().readAllBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();

                final int chunkSize = encoderModel.getLine();
                final int overlap = encoderModel.getOverlapLine();

                final int step = chunkSize - overlap;
                for (int page = 0, chunkIndex = 1; page < pdf.getNumberOfPages(); page++) {
                    int pageNumber = page + 1;
                    stripper.setStartPage(pageNumber);
                    stripper.setEndPage(pageNumber);
                    String pageText = stripper.getText(pdf);
                    if (pageText == null || pageText.isBlank()) {
                        continue;
                    }
                    String[] lines = pageText.split("\\R");
                    // Clean lines first
                    List<String> validLines = new ArrayList<>();
                    for (String line : lines) {
                        String text = normalizeText(line);
                        if (!text.isBlank()) {
                            validLines.add(text);
                        }
                    }
                    // Create chunks with 1-line overlap
                    for (int start = 0 ; start < validLines.size(); start += step) {

                        int end = Math.min(start + chunkSize, validLines.size());
                        String chunk = String.join("\n", validLines.subList(start, end));

                        // Safety check before TextSegment
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
                }
                return segments;
            }

        } catch (IOException exception) {
            log.error("Error while reading PDF: {}", encoderModel.getFileName(), exception);
            throw new FileReadError("Error While Reading PDF File");

        } catch (Exception exception) {
            log.error("Unexpected error while encoding PDF: {}", encoderModel.getFileName(), exception);
            throw new InternalServerErrorException("Internal Server Error");
        }
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
