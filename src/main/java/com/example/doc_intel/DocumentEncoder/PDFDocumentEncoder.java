package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.DTO.EncoderModel;
import com.example.doc_intel.Utils.EncoderUtils;
import com.example.doc_intel.Exceptions.FileReadError;
import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.Exceptions.InternalServerErrorException;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
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

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    @Override
    public List<TextSegment> encode(@NonNull final EncoderModel encoderModel) {
        log.info("Using PDF Encoder");

        try {
            List<TextSegment> segments = new ArrayList<>();
            String fileName = encoderModel.getFileName();
            try (PDDocument pdf = Loader.loadPDF(encoderModel.getFileStream().readAllBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();

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
                        String text = EncoderUtils.normalizeText(line);
                        if (!text.isBlank()) {
                            validLines.add(text);
                        }
                    }
                    chunkIndex = EncoderUtils.addPageChunks(segments, validLines, fileName, pageNumber, encoderModel, chunkIndex, embeddingModel);
                }
                return segments;
            }

        } catch (IOException exception) {
            log.error("Error while reading PDF: {}", encoderModel.getFileName(), exception);
            throw new FileReadError("Error While Reading PDF File", ErrorCode.PdfFileReadFailed);

        } catch (Exception exception) {
            log.error("Unexpected error while encoding PDF: {}", encoderModel.getFileName(), exception);
            throw new InternalServerErrorException("Internal Server Error", ErrorCode.DocumentParserFailed);
        }
    }

}
