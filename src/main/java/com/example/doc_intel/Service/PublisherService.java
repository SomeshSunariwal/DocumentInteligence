package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.DocumentProcessResponseDTO;
import com.example.doc_intel.DTO.FileRequestDTO;
import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.Kafka.KafkaProducer;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PublisherService {

    private final KafkaProducer kafkaProducer;

    public PublisherService(@NonNull KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public DocumentProcessResponseDTO publishDocument(@NonNull FileRequestDTO fileRequestDTO) {
        // Publish Event
        kafkaProducer.publish(new KafkaEventDTO<String[]>(UUID.randomUUID(), fileRequestDTO.getMessage()));
        return new DocumentProcessResponseDTO(null, null);
    }
}
