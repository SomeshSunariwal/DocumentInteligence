package com.example.doc_intel.Repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Component;

import com.example.doc_intel.Entity.DocumentEntity;

import lombok.NonNull;

@Component
public interface DocumentsRepository extends JpaRepository<DocumentEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DocumentEntity> findForUpdateByDocumentIdAndUser_EmailAndIsActiveTrue(@NonNull UUID documentId,
                                                                                @NonNull String email);

    Optional<DocumentEntity> findByDocumentIdAndUser_EmailAndIsActiveTrue(@NonNull UUID documentId,
                                                                          @NonNull String email);

    Page<DocumentEntity> findByUser_EmailAndIsActiveTrue(@NonNull String email, Pageable pageable);

}
