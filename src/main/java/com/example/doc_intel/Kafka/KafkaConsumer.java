package com.example.doc_intel.Kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.Service.DocumentProcessorService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class KafkaConsumer {

    private final DocumentProcessorService documentProcessorService;

    @KafkaListener(
        topics = Constants.DOCUMENT_EVENT,
        groupId = "document-processing-group"
    )
    public void consumer(KafkaEventDTO event) {
        log.info("Processing Event Id: {}", event.getEventId());
        documentProcessorService.processDocumentFromKafka(event);
        log.info("Processed Event Id: {}", event.getEventId());
    }

}
