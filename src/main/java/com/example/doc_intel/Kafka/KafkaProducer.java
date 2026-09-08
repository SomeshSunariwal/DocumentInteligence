package com.example.doc_intel.Kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.KafkaEventDTO;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, Object> template;

    public KafkaProducer(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    public <T> void publish(KafkaEventDTO<T> event) {
        template.send(Constants.DOCUMENT_EVENT, event);
    }
}
