package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.DocumentUploadResponseDTO;
import com.example.doc_intel.DTO.FileRequestDTO;
import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.Kafka.KafkaProducer;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PublisherService {

    private final KafkaProducer kafkaProducer;

    public PublisherService(@NonNull KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public DocumentUploadResponseDTO publishDocument(@NonNull FileRequestDTO fileRequestDTO) {
        // Publish Event
        kafkaProducer.publish(new KafkaEventDTO<String[]>(UUID.randomUUID(), fileRequestDTO.getMessage()));
        return new DocumentUploadResponseDTO(null, null);
    }
}
