package com.example.doc_intel.DocumentEncoder;

import dev.langchain4j.data.segment.TextSegment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentEncoder {
    List<TextSegment> encode(MultipartFile file) ;
}
