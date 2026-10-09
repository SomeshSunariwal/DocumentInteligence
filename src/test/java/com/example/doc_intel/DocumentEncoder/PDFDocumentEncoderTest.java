package com.example.doc_intel.DocumentEncoder;

import com.example.doc_intel.DTO.EncoderModel;
import com.example.doc_intel.Utils.EncoderUtils;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.Exceptions.FileReadError;
import com.example.doc_intel.Exceptions.InternalServerErrorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PDFDocumentEncoderTest {

    private final PDFDocumentEncoder encoder = new PDFDocumentEncoder();

    @Test
    void successfullyEncodesPagesAndSkipsBlankPages() throws Exception {
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed(anyString())).thenReturn(Response.from(Embedding.from(new float[] {1, 0})));
        ReflectionTestUtils.setField(encoder, "embeddingModel", model);
        byte[] pdf = pdfWithPages(List.of(
            List.of("A", "A", "A", "A", "A", "A", "A", "A", "A", "A"),
            List.of(), List.of("B", "B")));
        var segments = encoder.encode(settings(new ByteArrayInputStream(pdf)));
        assertEquals(List.of("A\nA\nA\nA\nA\nA\nA\nA", "A\nA\nA", "B\nB"),
            segments.stream().map(TextSegment::text).toList());
        assertEquals(List.of(1, 1, 3), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_DATA_PAGE_NUMBER)).toList());
        assertEquals(List.of(1, 8, 1), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_DATA_LINE_NUMBER)).toList());
        assertEquals(List.of(1, 2, 3), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_CHUNK_INDEX)).toList());
    }

    @Test
    void returnsNoSegmentsForBlankPdf() throws Exception {
        byte[] pdf = pdfWithPages(List.of(List.of()));
        assertTrue(encoder.encode(settings(new ByteArrayInputStream(pdf))).isEmpty());
    }

    @Test
    void filtersWhitespaceOnlyLinesFromNonblankPage() throws Exception {
        byte[] pdf = pdfWithPages(List.of(List.of("A", "   ", "B")));
        var segments = encoder.encode(settings(new ByteArrayInputStream(pdf)));
        assertEquals(List.of("A\nB"), segments.stream().map(TextSegment::text).toList());
    }

    @Test
    void rejectsNullEncoderModel() {
        assertThrows(NullPointerException.class, () -> encoder.encode(null));
    }

    private byte[] pdfWithPages(List<List<String>> pages) throws IOException {
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            for (List<String> lines : pages) {
                PDPage page = new PDPage();
                pdf.addPage(page);
                if (lines.isEmpty()) {
                    continue;
                }
                try (PDPageContentStream content = new PDPageContentStream(pdf, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(50, 700);
                    content.setLeading(16);
                    for (String line : lines) {
                        content.showText(line);
                        content.newLine();
                    }
                    content.endText();
                }
            }
            pdf.save(bytes);
            return bytes.toByteArray();
        }
    }

    @Test
    @SuppressEncoderLogs(PDFDocumentEncoder.class)
    void translatesStreamReadFailureToPdfReadError() throws Exception {
        InputStream stream = mock(InputStream.class);
        when(stream.readAllBytes()).thenThrow(new IOException("Read failed"));
        FileReadError exception = assertThrows(FileReadError.class, () -> encoder.encode(settings(stream)));
        assertEquals(ErrorCode.PdfFileReadFailed, exception.getErrorCode());
        assertEquals("Error While Reading PDF File", exception.getMessage());
    }

    @Test
    @SuppressEncoderLogs(PDFDocumentEncoder.class)
    void translatesMalformedPdfToPdfReadError() {
        FileReadError exception = assertThrows(FileReadError.class,
            () -> encoder.encode(settings(new ByteArrayInputStream(new byte[] {1, 2, 3}))));
        assertEquals(ErrorCode.PdfFileReadFailed, exception.getErrorCode());
    }

    @Test
    @SuppressEncoderLogs(PDFDocumentEncoder.class)
    void translatesEmbeddingFailureToDocumentParserError() throws Exception {
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed(anyString())).thenThrow(new IllegalStateException("Embedding failed"));
        ReflectionTestUtils.setField(encoder, "embeddingModel", model);
        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            pdf.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(pdf, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(50, 700);
                content.setLeading(16);
                for (int line = 0; line < 4; line++) {
                    content.showText("A tangent to a circle");
                    content.newLine();
                }
                content.endText();
            }
            pdf.save(bytes);
            InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
                () -> encoder.encode(settings(new ByteArrayInputStream(bytes.toByteArray()))));
            assertEquals(ErrorCode.DocumentParserFailed, exception.getErrorCode());
            assertEquals("Internal Server Error", exception.getMessage());
        }
    }

    private EncoderModel settings(InputStream stream) {
        return EncoderModel.builder().fileStream(stream).fileName("test.pdf")
            .userId("test").documentId("test").documentVersion(1)
            .maxLinesPerChunk(8).minLinesPerChunk(3).overlapLine(1).build();
    }

    @Test
    void waitsForThreeLinesBeforeSplittingAndEmitsShortPageRemainder() {
        var segments = splitLines(List.of("A", "B", "B", "C", "C", "C", "D", "D"));
        assertEquals(List.of("A\nB\nB", "C\nC\nC", "D\nD"),
            segments.stream().map(TextSegment::text).toList());
        assertEquals(List.of(1, 4, 7), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_DATA_LINE_NUMBER)).toList());
        assertEquals(List.of(1, 2, 3), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_CHUNK_INDEX)).toList());
    }

    @Test
    void refillsToEightLinesAfterSplitAtFive() {
        var segments = splitLines(List.of("A", "A", "A", "A", "A",
            "B", "B", "B", "B", "B", "B", "B", "B", "C", "C"));
        assertEquals(List.of("A\nA\nA\nA\nA", "B\nB\nB\nB\nB\nB\nB\nB", "C\nC"),
            segments.stream().map(TextSegment::text).toList());
        assertEquals(List.of(1, 6, 14), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_DATA_LINE_NUMBER)).toList());
    }

    @Test
    void emitsOneLineAtPageEnd() {
        assertEquals(List.of("A"), splitLines(List.of("A")).stream().map(TextSegment::text).toList());
    }

    @Test
    void respectsEightLineBatchesWithoutOverlapAndKeepsRemainder() {
        var segments = splitLines(List.of("A", "A", "A", "A", "A", "A", "A", "A", "A", "A"));
        assertEquals(List.of("A\nA\nA\nA\nA\nA\nA\nA", "A\nA"),
            segments.stream().map(TextSegment::text).toList());
    }

    private List<TextSegment> splitLines(List<String> lines) {
        return splitLines(lines, 0);
    }

    @Test
    void keepsOneOverlappingLineAndRefillsWindow() {
        var segments = splitLines(List.of("A", "A", "A", "A", "A",
            "B", "B", "B", "B", "B", "B", "B", "B"), 1);
        assertEquals(List.of("A\nA\nA\nA\nA", "A\nB\nB\nB\nB\nB\nB\nB", "B\nB"),
            segments.stream().map(TextSegment::text).toList());
        assertEquals(List.of(1, 5, 12), segments.stream()
            .map(segment -> segment.metadata().getInteger(Constants.META_DATA_LINE_NUMBER)).toList());
    }

    @Test
    void keepsTwoOverlappingLinesAfterSplitAtFive() {
        var segments = splitLines(List.of("A", "A", "A", "A", "A", "B", "B", "B", "B", "B", "B"), 2);
        assertEquals(List.of("A\nA\nA\nA\nA", "A\nA\nB\nB\nB\nB\nB\nB"),
            segments.stream().map(TextSegment::text).toList());
        assertEquals(4, segments.get(1).metadata().getInteger(Constants.META_DATA_LINE_NUMBER));
    }

    @Test
    void doesNotEmitAnExtraOverlapChunkAtPageEnd() {
        assertEquals(List.of("A\nA\nA"),
            splitLines(List.of("A", "A", "A"), 2).stream().map(TextSegment::text).toList());
    }

    @Test
    void rejectsOverlapThatPreventsProgress() {
        assertThrows(IllegalArgumentException.class, () -> splitLines(List.of("A", "A", "A"), 3));
        assertThrows(IllegalArgumentException.class, () -> splitLines(List.of("A", "A", "A"), -1));
    }

    private List<TextSegment> splitLines(List<String> lines, int overlap) {
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed(anyString())).thenAnswer(invocation -> {
            String text = invocation.getArgument(0);
            assertEquals(1, text.length(), "Only individual lines should be embedded");
            float[] vector = new float[4];
            vector[text.charAt(0) - 'A'] = 1;
            return Response.from(Embedding.from(vector));
        });
        ReflectionTestUtils.setField(encoder, "embeddingModel", model);
        EncoderModel settings = EncoderModel.builder().fileStream(new ByteArrayInputStream(new byte[0]))
            .fileName("test.pdf").userId("test").documentId("test").documentVersion(1)
            .maxLinesPerChunk(8).minLinesPerChunk(3).overlapLine(overlap).build();
        List<TextSegment> segments = new ArrayList<>();
        EncoderUtils.addPageChunks(segments, lines, "test.pdf", 1, settings, 1, model);
        return segments;
    }

    @Test
    void canCorrelateArrowSymbols() {
        Boolean correlated = EncoderUtils.areLinesCorrelated("Line → ➔ ⇘", "A line and a tangent to a circle", (EmbeddingModel) ReflectionTestUtils.getField(encoder, "embeddingModel"));
        assertTrue(correlated != null);
    }

    @Test
    void removesInvisibleOnlyExtractedLines() {
        String normalized = ReflectionTestUtils.invokeMethod(EncoderUtils.class, "normalizeText",
            "\u00A0\u2007\u202F\u200B\uFEFF\u0000\t");

        assertEquals("", normalized);
    }

    @Test
    void removesUnmappedFontGlyphsFromPdf() {
        String normalized = ReflectionTestUtils.invokeMethod(EncoderUtils.class, "normalizeText", "\uF0E8 \uF0F8");
        assertEquals("", normalized);
        Boolean correlated = EncoderUtils.areLinesCorrelated("\uF0E8 \uF0F8", "A tangent to a circle", (EmbeddingModel) ReflectionTestUtils.getField(encoder, "embeddingModel"));
        assertTrue(correlated);
    }

    @Test
    void preservesMathTextWhileCleaningExtractedCharacters() {
        String normalized = ReflectionTestUtils.invokeMethod(EncoderUtils.class, "normalizeText",
            "\uFEFF  Circle\u00A0\u2007radius\t= 5\u200B cm \u0000");

        assertEquals("Circle radius = 5 cm", normalized);
    }

    @Test
    void skipsCorrelationEmbeddingForInvisibleOnlyText() {
        Boolean blankLine = EncoderUtils.areLinesCorrelated("\u200B\u0000", "Circle radius is five centimetres", (EmbeddingModel) ReflectionTestUtils.getField(encoder, "embeddingModel"));
        Boolean blankChunk = EncoderUtils.areLinesCorrelated("Circle radius is five centimetres", "\uFEFF\u00A0", (EmbeddingModel) ReflectionTestUtils.getField(encoder, "embeddingModel"));

        assertTrue(blankLine);
        assertTrue(blankChunk);
    }
}
