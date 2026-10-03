package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.Kafka.KafkaProducer;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PublisherService {

    private final KafkaProducer kafkaProducer;

    public void publishDocument(@NonNull KafkaEventDTO kafkaEventDTO) {
        kafkaProducer.publish(kafkaEventDTO);
    }
}
