package com.example.doc_intel.Service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Enums.DocumentStatus;
import com.example.doc_intel.Repository.DocumentsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentProcessingStatusService {

    private final DocumentsRepository documentsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(UUID documentId) {
        updateStatus(documentId, DocumentStatus.PROCESSING);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID documentId) {
        updateStatus(documentId, DocumentStatus.FAILED);
    }

    private void updateStatus(UUID documentId, DocumentStatus status) {
        documentsRepository.findByDocumentIdAndIsActiveTrue(documentId)
            .ifPresent(document -> document.setStatus(status));
    }
}
