package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.Exceptions.FileReadError;
import com.example.doc_intel.DTO.EncoderModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TextFileDocumentEncoderTest {

    @Test
    @SuppressEncoderLogs(TextFileDocumentEncoder.class)
    void translatesStreamReadFailureAndClosesReader() throws Exception {
        InputStream stream = mock(InputStream.class);
        when(stream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("Read failed"));
        var encoder = new TextFileDocumentEncoder();
        FileReadError exception = assertThrows(FileReadError.class, () -> encoder.encode(settings(stream)));
        assertEquals(ErrorCode.TextFileReadFailed, exception.getErrorCode());
        assertEquals("Error While Reading File", exception.getMessage());
        verify(stream).close();
    }

    @Test
    @SuppressEncoderLogs(TextFileDocumentEncoder.class)
    void translatesEmbeddingFailureAndClosesReader() throws Exception {
        InputStream stream = mock(InputStream.class);
        byte[] text = "A\nA\nA\nA".getBytes(StandardCharsets.UTF_8);
        when(stream.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(text, 0, buffer, invocation.getArgument(1), text.length);
            return text.length;
        }).thenReturn(-1);
        var encoder = new TextFileDocumentEncoder();
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed(anyString())).thenThrow(new IllegalStateException("Embedding failed"));
        ReflectionTestUtils.setField(encoder, "embeddingModel", model);
        FileReadError exception = assertThrows(FileReadError.class, () -> encoder.encode(settings(stream)));
        assertEquals(ErrorCode.TextFileReadFailed, exception.getErrorCode());
        verify(stream).close();
    }

    @Test
    @SuppressEncoderLogs(TextFileDocumentEncoder.class)
    void translatesInvalidOverlapToTextReadError() {
        FileReadError exception = assertThrows(FileReadError.class, () -> encode("A\nA\nA", 3));
        assertEquals(ErrorCode.TextFileReadFailed, exception.getErrorCode());
    }

    private EncoderModel settings(InputStream stream) {
        return EncoderModel.builder().fileStream(stream).fileName("test.txt")
            .userId("test").documentId("test").documentVersion(1)
            .maxLinesPerChunk(8).minLinesPerChunk(3).overlapLine(1).build();
    }

    @Test
    void splitsAdjacentLinesAndRefillsWithOneLineOverlap() {
        var segments = encode("A\nA\nA\nA\nA\nB\nB\nB\nB\nB\nB\nB\nB", 1);
        assertEquals(List.of("A\nA\nA\nA\nA", "A\nB\nB\nB\nB\nB\nB\nB", "B\nB"),
            segments.stream().map(TextSegment::text).toList());
        assertEquals(List.of(1, 5, 12), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_DATA_LINE_NUMBER)).toList());
    }

    @Test
    void supportsTwoLineOverlap() {
        var segments = encode("A\nA\nA\nA\nA\nB\nB\nB\nB\nB\nB", 2);
        assertEquals(List.of("A\nA\nA\nA\nA", "A\nA\nB\nB\nB\nB\nB\nB"),
            segments.stream().map(TextSegment::text).toList());
    }

    @Test
    void waitsForThreeLinesAndAllowsShortFinalChunk() {
        var segments = encode("A\nB\nB\nC\nC\nC\nD\nD", 0);
        assertEquals(List.of("A\nB\nB", "C\nC\nC", "D\nD"),
            segments.stream().map(TextSegment::text).toList());
    }

    @Test
    void resetsLineNumbersAndOverlapAtVirtualPageBoundary() {
        var segments = encode("A\n".repeat(26), 1);
        assertEquals(List.of(1, 1, 1, 1, 2), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_DATA_PAGE_NUMBER)).toList());
        assertEquals(List.of(1, 8, 15, 22, 1), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_DATA_LINE_NUMBER)).toList());
        assertEquals(List.of(1, 2, 3, 4, 5), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_CHUNK_INDEX)).toList());
        assertEquals("A", segments.get(4).text());
    }

    @Test
    void normalizesInvisibleTextAndPreservesLineWrapping() {
        var segments = encode("\uF0E8\u200B\n\n" + "A".repeat(80), 1);
        assertEquals(List.of("A".repeat(78) + "\nAA"), segments.stream().map(TextSegment::text).toList());
    }

    private List<TextSegment> encode(String text, int overlap) {
        var encoder = new TextFileDocumentEncoder();
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        when(embeddingModel.embed(anyString())).thenAnswer(invocation -> {
            String line = invocation.getArgument(0);
            assertEquals(1, line.length(), "Similarity must compare individual lines");
            float[] vector = new float[4];
            vector[line.charAt(0) - 'A'] = 1;
            return Response.from(Embedding.from(vector));
        });
        ReflectionTestUtils.setField(encoder, "embeddingModel", embeddingModel);
        return encoder.encode(EncoderModel.builder()
            .fileStream(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)))
            .fileName("sample.txt").userId("test").documentId("test").documentVersion(1)
            .maxLinesPerChunk(8).minLinesPerChunk(3).overlapLine(overlap).build());
    }
}
