package com.example.doc_intel.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.example.doc_intel.Entity.DocumentEntity;

import lombok.NonNull;

@Component
public interface DocumentsRepository extends JpaRepository<DocumentEntity, UUID> {

    Optional<DocumentEntity> findByDocumentIdAndIsActiveTrue(@NonNull UUID documentId);

    Optional<DocumentEntity> findByDocumentIdAndUser_EmailAndIsActiveTrue(@NonNull UUID documentId,
                                                                          @NonNull String email);

    List<DocumentEntity> findByUser_EmailAndIsActiveTrue(@NonNull String email);

    Optional<DocumentEntity> findByDocumentIdAndVersionAndIsActiveTrue(@NonNull UUID documentId, @NonNull Integer version);
}
