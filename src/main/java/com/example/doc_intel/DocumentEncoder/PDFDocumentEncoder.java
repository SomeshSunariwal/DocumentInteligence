package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Exceptions.FileReadError;
import com.example.doc_intel.Exceptions.InternalServerErrorException;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PDFDocumentEncoder implements DocumentEncoder {

    @Override
    public List<TextSegment> encode(final MultipartFile file) {
        log.info("Using PDF Encoder");
        try {
            List<TextSegment> segments = new ArrayList<>();
            String fileName = file.getOriginalFilename();
            try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {

                for (int page = 0; page < pdf.getNumberOfPages(); page++) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    stripper.setStartPage(page + 1);
                    stripper.setEndPage(page + 1);
                    String pageText = stripper.getText(pdf);
                    String[] lines = pageText.split("\\R");

                    for (int line = 0; line < lines.length; line++) {
                        String text = lines[line].trim();
                        if (text.isEmpty()) {
                            continue;
                        }
                        Metadata metadata = new Metadata();
                        metadata.put(Constants.FILE_NAME, fileName);
                        metadata.put(Constants.PAGE_NUMBER, page + 1);
                        metadata.put(Constants.LINE_NUMBER, line + 1);
                        metadata.put(Constants.TEXT, text);
                        TextSegment segment = TextSegment.from(text, metadata);
                        segments.add(segment);
                    }
                }
                return segments;
            }
        } catch (IOException exception) {
            throw new FileReadError("Error While Reading PDF File");
        } catch (Exception e) {
            throw new InternalServerErrorException("Internal Server Error");
        }
    }
}
