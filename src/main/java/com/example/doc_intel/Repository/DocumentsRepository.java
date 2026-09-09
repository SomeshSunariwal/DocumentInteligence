package com.example.doc_intel.Repository;

import com.example.doc_intel.Entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface DocumentsRepository extends JpaRepository<DocumentEntity, Integer> {
}
