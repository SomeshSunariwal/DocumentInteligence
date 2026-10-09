package com.example.doc_intel.Utils;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.EncoderModel;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;

import java.util.List;

public final class EncoderUtils {

    private EncoderUtils() {
    }

    public static int addPageChunks(List<TextSegment> segments, List<String> lines, String fileName, int pageNumber,
                              EncoderModel encoderModel, int firstChunkIndex, EmbeddingModel embeddingModel) {
        int chunkIndex = firstChunkIndex;
        int maxLines = encoderModel.getMaxLinesPerChunk();
        int minLines = encoderModel.getMinLinesPerChunk();
        int overlap = encoderModel.getOverlapLine();
        if (minLines <= 0 || maxLines < minLines) {
            throw new IllegalArgumentException("Chunk limits must satisfy 0 < minLinesPerChunk <= maxLinesPerChunk");
        }
        if (overlap < 0 || overlap >= minLines) {
            throw new IllegalArgumentException("overlapLine must satisfy 0 <= overlapLine < minLinesPerChunk");
        }
        int chunkStart = 0;
        while (chunkStart < lines.size()) {
            int chunkEnd = Math.min(chunkStart + maxLines, lines.size());
            for (int line = chunkStart + minLines; line < chunkEnd; line++) {
                if (!areLinesCorrelated(lines.get(line - 1), lines.get(line), embeddingModel)) {
                    chunkEnd = line;
                    break;
                }
            }
            addSegment(segments, lines.subList(chunkStart, chunkEnd), fileName, pageNumber,
                chunkStart + 1, encoderModel, chunkIndex++);
            if (chunkEnd == lines.size()) {
                break;
            }
            // Keep the trailing overlap and refill from this page after every split.
            chunkStart = chunkEnd - overlap;
        }
        return chunkIndex;
    }

    /**
     * Uses LangChain4j embeddings and cosine similarity to compare adjacent lines.
     */
    public static boolean areLinesCorrelated(String firstLine, String secondLine, EmbeddingModel embeddingModel) {
        firstLine = normalizeText(firstLine);
        secondLine = normalizeText(secondLine);
        if (firstLine.isBlank() || secondLine.isBlank()) {
            return true;
        }
        Embedding firstEmbedding = embeddingModel.embed(firstLine).content();
        Embedding secondEmbedding = embeddingModel.embed(secondLine).content();
        return CosineSimilarity.between(firstEmbedding, secondEmbedding) >= Constants.CORRELATION_THRESHOLD;
    }

    private static void addSegment(List<TextSegment> segments, List<String> lines, String fileName, int pageNumber,
                            int startLine, EncoderModel encoderModel, int chunkIndex) {
        String chunk = String.join("\n", lines);
        if (chunk.isBlank()) {
            return;
        }
        Metadata metadata = new Metadata();
        metadata.put(Constants.META_DATA_FILE_NAME, fileName);
        metadata.put(Constants.META_DATA_PAGE_NUMBER, pageNumber);
        metadata.put(Constants.META_DATA_LINE_NUMBER, startLine);
        metadata.put(Constants.META_USER_ID, encoderModel.getUserId());
        metadata.put(Constants.META_DOCUMENT_VERSION, encoderModel.getDocumentVersion());
        metadata.put(Constants.META_DOCUMENT_ID, encoderModel.getDocumentId());
        metadata.put(Constants.META_CHUNK_INDEX, chunkIndex);
        segments.add(TextSegment.from(chunk, metadata));
    }

    public static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replaceAll("[\\p{Z}\\s]+", " ")
            .replaceAll("[\\p{C}]", "")
            .strip();
    }
}
