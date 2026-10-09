package com.example.doc_intel.Utils;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.DTO.CustomRange.RangeFilterImp;
import com.example.doc_intel.Enums.FileExtensions;
import com.example.doc_intel.Exceptions.OpenSearchException.UnsupportedFilterException;
import com.example.doc_intel.Exceptions.UnSupportedFileException;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.logical.And;
import dev.langchain4j.store.embedding.filter.logical.Or;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilsTest {

    @Test
    void recognizesWordExtensions() {
        assertEquals(FileExtensions.DOC, Utils.getExtension(fileNamed("document.DOC")));
        assertEquals(FileExtensions.DOCX, Utils.getExtension(fileNamed("document.docx")));
        assertEquals(FileExtensions.DOCX, Utils.getExtension(fileNamed("document.docs")));
    }

    @Test
    void getExtensionReturnsPdfForPdfFile() {
        MockMultipartFile file = fileNamed("document.pdf");
        assertEquals(FileExtensions.PDF, Utils.getExtension(file));
    }

    @Test
    void getExtensionReturnsTxtForUppercaseExtension() {
        MockMultipartFile file = fileNamed("NOTES.TXT");
        assertEquals(FileExtensions.TXT, Utils.getExtension(file));
    }

    @Test
    void getExtensionReturnsNullForUnsupportedExtension() {
        MockMultipartFile file = fileNamed("document.zip");
        assertNull(Utils.getExtension(file));
    }

    @Test
    void getExtensionThrowsForMissingFile() {
        UnSupportedFileException exception =
            assertThrows(UnSupportedFileException.class, () -> Utils.getExtension(null));

        assertEquals(ErrorCode.FileMissing, exception.getErrorCode());
        assertEquals("No File Available", exception.getMessage());
    }

    @Test
    void getExtensionThrowsWhenFilenameIsMissing() {
        MockMultipartFile file =
            new MockMultipartFile("file", null, "text/plain", "content".getBytes());
        UnSupportedFileException exception =
            assertThrows(UnSupportedFileException.class, () -> Utils.getExtension(file));
        assertEquals(ErrorCode.FileExtensionMalformed, exception.getErrorCode());
    }

    @Test
    void getExtensionThrowsWhenFilenameHasNoExtension() {
        MockMultipartFile file = fileNamed("README");
        UnSupportedFileException exception =
            assertThrows(UnSupportedFileException.class, () -> Utils.getExtension(file));
        assertEquals(ErrorCode.FileExtensionMalformed, exception.getErrorCode());
    }

    @Test
    void getFilterFieldMapsStringMetadataKeysToKeywordFields() {
        assertEquals("metadata.userId.keyword", Utils.getFilterField(Constants.META_USER_ID));
        assertEquals("metadata.documentId.keyword", Utils.getFilterField(Constants.META_DOCUMENT_ID));
        assertEquals("metadata.fileName.keyword", Utils.getFilterField(Constants.META_DATA_FILE_NAME));
    }

    @Test
    void getFilterFieldMapsNumericMetadataKeys() {
        assertEquals("metadata.chunkIndex", Utils.getFilterField(Constants.META_CHUNK_INDEX));
        assertEquals("metadata.documentVersion", Utils.getFilterField(Constants.META_DOCUMENT_VERSION));
        assertEquals("metadata.lineNumber", Utils.getFilterField(Constants.META_DATA_LINE_NUMBER));
        assertEquals("metadata.pageNumber", Utils.getFilterField(Constants.META_DATA_PAGE_NUMBER));
    }

    @Test
    void getFilterFieldThrowsForUnsupportedKey() {
        UnsupportedFilterException exception =
            assertThrows(UnsupportedFilterException.class, () -> Utils.getFilterField("unknownField"));
        assertEquals(ErrorCode.UnsupportedMetadataFilter, exception.getErrorCode());
        assertEquals("Unsupported metadata field: unknownField", exception.getMessage());
    }

    @Test
    void getExtraContextFromOpenSearchReturnsNullForNoMatches() {
        assertNull(Utils.getExtraContextFromOpenSearch(List.of()));
    }

    @Test
    void getExtraContextFromOpenSearchBuildsDocumentVersionAndChunkRangeFilters() {
        Filter filter = Utils.getExtraContextFromOpenSearch(List.of(
            match("document-1", 2, 4),
            match("document-1", 2, 7),
            match("document-1", 3, 1)
        ));

        List<RangeFilterImp> ranges = collectRanges(filter);
        assertEquals(3, ranges.size());
        assertEquals(List.of(1L, 3L, 6L), ranges.stream().map(RangeFilterImp::min).sorted().toList());
        assertEquals(List.of(2L, 5L, 8L), ranges.stream().map(RangeFilterImp::max).sorted().toList());
        assertEquals(3, countEqualsTo(filter, Constants.META_DOCUMENT_ID, "document-1"));
        assertEquals(1, countEqualsTo(filter, Constants.META_DOCUMENT_VERSION, 3));
    }

    @Test
    void getExtraContextFromOpenSearchDeduplicatesChunksAndSeparatesDocumentsAndVersions() {
        Filter filter = Utils.getExtraContextFromOpenSearch(List.of(
            match("document-1", 1, 1),
            match("document-1", 1, 1),
            match("document-1", 2, 5),
            match("document-2", 1, 2)
        ));

        List<RangeFilterImp> ranges = collectRanges(filter);
        assertEquals(3, ranges.size());
    }

    private EmbeddingMatch<TextSegment> match(String documentId, int version, int chunkIndex) {
        Metadata metadata = new Metadata()
            .put(Constants.META_DOCUMENT_ID, documentId)
            .put(Constants.META_DOCUMENT_VERSION, version)
            .put(Constants.META_CHUNK_INDEX, chunkIndex);
        return new EmbeddingMatch<>(1.0, documentId + "-" + version + "-" + chunkIndex, null,
            TextSegment.from("chunk", metadata));
    }

    private List<RangeFilterImp> collectRanges(Filter filter) {
        if (filter instanceof RangeFilterImp range) {
            return List.of(range);
        }
        if (filter instanceof And and) {
            ArrayList<RangeFilterImp> ranges = new ArrayList<>(collectRanges(and.left()));
            ranges.addAll(collectRanges(and.right()));
            return ranges;
        }
        if (filter instanceof Or or) {
            ArrayList<RangeFilterImp> ranges = new ArrayList<>(collectRanges(or.left()));
            ranges.addAll(collectRanges(or.right()));
            return ranges;
        }
        return List.of();
    }

    private int countEqualsTo(Filter filter, String key, Object value) {
        if (filter instanceof IsEqualTo isEqualTo) {
            return isEqualTo.key().equals(key) && isEqualTo.comparisonValue().equals(value) ? 1 : 0;
        }
        if (filter instanceof And and) {
            return countEqualsTo(and.left(), key, value) + countEqualsTo(and.right(), key, value);
        }
        if (filter instanceof Or or) {
            return countEqualsTo(or.left(), key, value) + countEqualsTo(or.right(), key, value);
        }
        return 0;
    }

    private MockMultipartFile fileNamed(String filename) {
        return new MockMultipartFile("file", filename, "application/octet-stream", new byte[0]);
    }
}
