package com.example.doc_intel.DocumentEncoder;

import dev.langchain4j.data.segment.TextSegment;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentEncoder {
    List<TextSegment> encode(@NonNull final MultipartFile file, @NonNull final String userId,
                             @NonNull final Integer version);
}
