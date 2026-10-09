package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.DTO.EncoderModel;
import com.example.doc_intel.Exceptions.EncoderException.WordDocumentEncoderException;
import com.example.doc_intel.Exceptions.FileReadError;
import dev.langchain4j.data.segment.TextSegment;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Uses the text encoder's virtual pages and shared chunking for extracted Word text.
 */
@Slf4j
@Component
public class WordDocumentEncoder implements DocumentEncoder {

    private final TextFileDocumentEncoder textFileDocumentEncoder;

    public WordDocumentEncoder(TextFileDocumentEncoder textFileDocumentEncoder) {
        this.textFileDocumentEncoder = textFileDocumentEncoder;
    }

    @Override
    public List<TextSegment> encode(@NonNull final EncoderModel encoderModel) {
        log.info("Using Word Encoder");
        try (InputStream stream = encoderModel.getFileStream()) {
            String text = getText(encoderModel, stream);
            EncoderModel textModel = EncoderModel.builder()
                .fileStream(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)))
                .fileName(encoderModel.getFileName()).userId(encoderModel.getUserId())
                .documentId(encoderModel.getDocumentId()).documentVersion(encoderModel.getDocumentVersion())
                .maxLinesPerChunk(encoderModel.getMaxLinesPerChunk())
                .minLinesPerChunk(encoderModel.getMinLinesPerChunk()).overlapLine(encoderModel.getOverlapLine())
                .build();
            return textFileDocumentEncoder.encode(textModel);
        } catch (Exception exception) {
            log.error("Error while encoding Word file: {}", encoderModel.getFileName(), exception);
            throw new FileReadError("Error While Reading Word File", ErrorCode.WordFileReadFailed);
        }
    }

    private static String getText(@NonNull EncoderModel encoderModel,
                                  InputStream stream) {
        String text;
        if (encoderModel.getFileName().toLowerCase(Locale.ROOT).endsWith(".doc")) {
            try (HWPFDocument document = new HWPFDocument(stream);
                 WordExtractor extractor = new WordExtractor(document)) {
                text = extractor.getText();
            } catch (Exception e) {
                throw new WordDocumentEncoderException("Document Not Processable",
                    ErrorCode.WordDocumentProcessingFailed);
            }
        } else {
            try (XWPFDocument document = new XWPFDocument(stream);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                text = extractor.getText();
            } catch (Exception e) {
                throw new WordDocumentEncoderException("Document Not Processable",
                    ErrorCode.WordDocumentXProcessingFailed);
            }
        }
        return text;
    }
}
