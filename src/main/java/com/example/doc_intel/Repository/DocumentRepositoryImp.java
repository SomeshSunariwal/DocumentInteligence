package com.example.doc_intel.Repository;

import com.example.doc_intel.Entity.DocumentEntity;
import org.springframework.stereotype.Component;

@Component
public class DocumentRepositoryImp {

    private DocumentsRepository documentsRepository;

    public DocumentRepositoryImp(DocumentsRepository documentsRepository) {
        this.documentsRepository = documentsRepository;
    }

    public void add(DocumentEntity documentEntity) {
        documentsRepository.save(documentEntity);
    }
}
