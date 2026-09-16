package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Enums.DocumentStatus;
import com.example.doc_intel.Exceptions.DBExceptions.DocumentNotExistException;
import com.example.doc_intel.Kafka.KafkaProducer;
import com.example.doc_intel.Repository.DocumentsRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PublisherService {

    private final KafkaProducer kafkaProducer;

    private final DocumentsRepository documentsRepository;

    public void publishDocument(@NonNull KafkaEventDTO kafkaEventDTO) {
        // Publish Event
        kafkaProducer.publish(kafkaEventDTO);
        updateDocumentStatus(kafkaEventDTO);
    }

    private void  updateDocumentStatus(@NonNull KafkaEventDTO kafkaEventDTO) {
        Optional<DocumentEntity> documentEntityOptional = documentsRepository.findByDocumentId(
            kafkaEventDTO.getDocumentId());
        // Certainly not possible but good to have
        if (documentEntityOptional.isEmpty()) {
            throw new DocumentNotExistException("Error While Document Processing");
        }
        DocumentEntity documentEntity = documentEntityOptional.get();
        // Update Document Status
        documentEntity.setStatus(DocumentStatus.PROCESSING);
    }
}
