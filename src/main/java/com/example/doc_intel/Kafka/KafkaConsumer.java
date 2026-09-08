package com.example.doc_intel.Kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.KafkaEventDTO;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaConsumer {

    @KafkaListener(
            topics = Constants.DOCUMENT_EVENT,
            groupId = "document-processing-group"
    )
    public void consume(KafkaEventDTO<JsonNode> event) {
        log.info("Event ID: " + event.getId());
        log.info("Message: " + event.getMessage());
    }
}
