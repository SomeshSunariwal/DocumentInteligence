package com.example.doc_intel.Service;

import com.example.doc_intel.dto.DocumentProcessResponseDTO;
import com.example.doc_intel.dto.FileRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class PublisherService {
    public DocumentProcessResponseDTO publishDocument(FileRequestDTO fileRequestDTO) {
        return new DocumentProcessResponseDTO(null, null);
    }
}
