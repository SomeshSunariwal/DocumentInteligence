package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.DTO.EncoderModel;
import com.example.doc_intel.Enums.FileExtensions;
import com.example.doc_intel.Exceptions.FileReadError;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WordDocumentEncoderTest {

    private final TextFileDocumentEncoder textEncoder = new TextFileDocumentEncoder();

    private final WordDocumentEncoder encoder = new WordDocumentEncoder(textEncoder);

    @Test
    void extractsDocxAndAppliesRollingChunksWithOverlap() throws Exception {
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed(anyString())).thenAnswer(invocation -> {
            String line = invocation.getArgument(0);
            return Response.from(Embedding.from(line.equals("A") ? new float[] {1, 0} : new float[] {0, 1}));
        });
        ReflectionTestUtils.setField(textEncoder, "embeddingModel", model);
        var segments = encoder.encode(settings(new ByteArrayInputStream(docx(
            List.of("A", "A", "A", "A", "A", "B", "B", "B", "B", "B", "B"))), "sample.docx"));
        assertEquals(List.of("A\nA\nA\nA\nA", "A\nB\nB\nB\nB\nB\nB"),
            segments.stream().map(segment -> segment.text()).toList());
        assertEquals(5, segments.get(1).metadata().getInteger(Constants.META_DATA_LINE_NUMBER));
        assertEquals("sample.docx", segments.get(0).metadata().getString(Constants.META_DATA_FILE_NAME));
        assertEquals("test", segments.get(0).metadata().getString(Constants.META_DOCUMENT_ID));
    }

    @Test
    void extractsTablesAndSupportsDocsAlias() throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            document.createTable(1, 1).getRow(0).getCell(0).setText("Table content");
            document.write(bytes);
            var segments = encoder.encode(settings(new ByteArrayInputStream(bytes.toByteArray()), "sample.docs"));
            assertEquals(1, segments.size());
            assertTrue(segments.get(0).text().contains("Table content"));
        }
    }

    @Test
    void returnsNoChunksForEmptyWordDocument() throws Exception {
        assertTrue(encoder.encode(settings(new ByteArrayInputStream(docx(List.of())), "sample.docx")).isEmpty());
    }

    @Test
    @SuppressEncoderLogs(WordDocumentEncoder.class)
    void translatesCorruptDocAndDocxToWordReadError() {
        for (String fileName : List.of("sample.doc", "sample.docx")) {
            FileReadError error = assertThrows(FileReadError.class,
                () -> encoder.encode(settings(new ByteArrayInputStream(new byte[] {1, 2, 3}), fileName)));
            assertEquals(ErrorCode.WordFileReadFailed, error.getErrorCode());
            assertEquals("Error While Reading Word File", error.getMessage());
        }
    }

    @Test
    @SuppressEncoderLogs(WordDocumentEncoder.class)
    void translatesInputReadFailure() throws Exception {
        InputStream stream = mock(InputStream.class);
        when(stream.read()).thenThrow(new IOException("Read failed"));
        when(stream.read(org.mockito.ArgumentMatchers.any(byte[].class),
            org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
            .thenThrow(new IOException("Read failed"));
        var error = assertThrows(FileReadError.class, () -> encoder.encode(settings(stream, "sample.docx")));
        assertEquals(ErrorCode.WordFileReadFailed, error.getErrorCode());
    }

    @Test
    void routesBothWordExtensionsToWordEncoder() {
        var pdfEncoder = mock(PDFDocumentEncoder.class);
        var factory = new DocumentEncoderFactory(textEncoder, pdfEncoder, encoder);
        assertSame(encoder, factory.getParser(FileExtensions.DOC));
        assertSame(encoder, factory.getParser(FileExtensions.DOCX));
        assertSame(textEncoder, factory.getParser(FileExtensions.TXT));
        assertSame(pdfEncoder, factory.getParser(FileExtensions.PDF));
    }

    private byte[] docx(List<String> lines) throws IOException {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            for (String line : lines) {
                document.createParagraph().createRun().setText(line);
            }
            document.write(bytes);
            return bytes.toByteArray();
        }
    }

    private EncoderModel settings(InputStream stream, String fileName) {
        return EncoderModel.builder().fileStream(stream).fileName(fileName)
            .userId("test").documentId("test").documentVersion(1)
            .maxLinesPerChunk(8).minLinesPerChunk(3).overlapLine(1).build();
    }
}
