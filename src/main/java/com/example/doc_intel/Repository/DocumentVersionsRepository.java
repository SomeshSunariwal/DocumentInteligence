package com.example.doc_intel.Repository;

import com.example.doc_intel.Entity.DocumentVersionEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionsRepository extends JpaRepository<DocumentVersionEntity, Integer> {

    Optional<DocumentVersionEntity> findFirstByDocument_DocumentIdAndDocument_IsActiveTrueOrderByDocumentVersionDesc(
        @NonNull UUID documentId);

    Optional<DocumentVersionEntity> findByDocument_DocumentIdAndDocumentVersionAndDocument_IsActiveTrue(
        @NonNull UUID documentId, @NonNull Integer documentVersion);

    List<DocumentVersionEntity> findByDocument_DocumentIdAndDocument_IsActiveTrueOrderByDocumentVersionDesc(
        @NonNull UUID documentId);

}
