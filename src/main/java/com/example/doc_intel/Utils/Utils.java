package com.example.doc_intel.Utils;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.CustomRange.RangeFilterImp;
import com.example.doc_intel.DTO.OpenSearchMetaDataDTO;
import com.example.doc_intel.Enums.FileExtensions;
import com.example.doc_intel.Exceptions.OpenSearchException.UnsupportedFilterException;
import com.example.doc_intel.Exceptions.UnAuthenticatedUser;
import com.example.doc_intel.Exceptions.UnSupportedFileException;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Slf4j
public class Utils {

    private static final List<FileExtensions> supportedTypes = Arrays.asList(FileExtensions.PDF, FileExtensions.TXT);

    public static Metadata convertToMetaData(OpenSearchMetaDataDTO openSearchMetaDataDTO) {
        Metadata metadata = new Metadata();
        metadata.put(Constants.META_DATA_FILE_NAME, openSearchMetaDataDTO.getFileName());
        metadata.put(Constants.META_DATA_LINE_NUMBER, openSearchMetaDataDTO.getLineNumber());
        metadata.put(Constants.META_DATA_PAGE_NUMBER, openSearchMetaDataDTO.getPageNumber());
        metadata.put(Constants.META_USER_ID, openSearchMetaDataDTO.getUserId());
        metadata.put(Constants.META_DOCUMENT_VERSION, openSearchMetaDataDTO.getDocumentVersion());
        metadata.put(Constants.META_DOCUMENT_ID, openSearchMetaDataDTO.getDocumentId());
        metadata.put(Constants.META_CHUNK_INDEX, openSearchMetaDataDTO.getChunkIndex());
        return metadata;
    }

    public static FileExtensions getExtension(MultipartFile file) {
        if (file == null) {
            throw new UnSupportedFileException("No File Available");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new UnSupportedFileException("File format is not proper");
        }

        String extension = filename
            .substring(filename.lastIndexOf('.') + 1)
            .toLowerCase();
        if (!supportedTypes.contains(FileExtensions.valueOf(extension.toUpperCase()))) {
            return null;
        }
        return FileExtensions.valueOf(extension.toUpperCase());
    }

    public static String getObjectKey(@NonNull String userName,
                                      @NonNull UUID documentUUID,
                                      @NonNull String fileName) {
        return userName + File.separator + documentUUID + File.separator + fileName;
    }

    public static String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getSubject();
        }
        throw new UnAuthenticatedUser("User is not authenticated");
    }

    public static String getFilterField(String key) {
        return switch (key) {

            // String values
            case Constants.META_USER_ID -> "metadata.userId.keyword";
            case Constants.META_DOCUMENT_ID -> "metadata.documentId.keyword";
            case Constants.META_DATA_FILE_NAME -> "metadata.fileName.keyword";

            // Integer Long values
            case Constants.META_CHUNK_INDEX -> "metadata.chunkIndex";
            case Constants.META_DOCUMENT_VERSION -> "metadata.documentVersion";
            case Constants.META_DATA_LINE_NUMBER -> "metadata.lineNumber";
            case Constants.META_DATA_PAGE_NUMBER -> "metadata.pageNumber";
            default -> throw new UnsupportedFilterException("Unsupported metadata field: " + key);
        };
    }

    public static Filter getExtraContextFromOpenSearch(@NonNull List<EmbeddingMatch<TextSegment>> matches) {

        // Maintaining the unique Doc and version id to avoid duplicates
        Map<String, Set<Integer>> documentChunks = new HashMap<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            Metadata metadata = match.embedded().metadata();
            String docId = metadata.getString(Constants.META_DOCUMENT_ID);
            Integer version = metadata.getInteger(Constants.META_DOCUMENT_VERSION);
            Integer chunkIndex = metadata.getInteger(Constants.META_CHUNK_INDEX);
            String key = docId + ":" + version;
            documentChunks
                .computeIfAbsent(key, k -> new HashSet<>())
                .add(chunkIndex);
        }

        List<Filter> filters = new ArrayList<>();
        for (Map.Entry<String, Set<Integer>> entry : documentChunks.entrySet()) {
            String[] parts = entry.getKey().split(":", 2);
            String documentId = parts[0];
            Integer version = Integer.valueOf(parts[1]);

            for (Integer chunkIndex : entry.getValue()) {
                // Previous + current + next range
                int minChunk = Math.max(1, chunkIndex - 1);
                int maxChunk = chunkIndex + 1;

                Filter filter = metadataKey(Constants.META_DOCUMENT_ID).isEqualTo(documentId)
                    .and(metadataKey(Constants.META_DOCUMENT_VERSION).isEqualTo(version))
                    .and(new RangeFilterImp(Constants.META_CHUNK_INDEX, (long) minChunk, (long) maxChunk));
                filters.add(filter);
            }
        }

        // Combine filters with OR
        if (filters.isEmpty()) return null;

        Filter result = filters.getFirst();
        for (int i = 1; i < filters.size(); i++) {
            result = result.or(filters.get(i));
        }
        return result;
    }
}
