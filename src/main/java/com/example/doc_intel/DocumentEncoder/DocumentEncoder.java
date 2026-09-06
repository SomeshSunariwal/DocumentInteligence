package com.example.doc_intel.DocumentEncoder;

import dev.langchain4j.data.document.Document;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentEncoder {
    Document encode(MultipartFile file) ;
}
