package com.example.doc_intel.Service;

import java.util.UUID;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.doc_intel.Enums.DocumentStatus;
import com.example.doc_intel.Repository.DocumentVersionsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentProcessingStatusService {

    private final DocumentVersionsRepository documentVersionsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(@NonNull UUID documentId, @NonNull Integer documentVersion) {
        updateStatus(documentId, DocumentStatus.PROCESSING, documentVersion);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(@NonNull UUID documentId, @NonNull Integer documentVersion) {
        updateStatus(documentId, DocumentStatus.FAILED, documentVersion);
    }

    private void updateStatus(@NonNull UUID documentId, @NonNull DocumentStatus status,
                              @NonNull Integer documentVersion) {
        documentVersionsRepository
            .findByDocument_DocumentIdAndDocumentVersionAndDocument_IsActiveTrue(documentId, documentVersion)
            .ifPresent(documentVersionEntity -> documentVersionEntity.setStatus(status));
    }
}
