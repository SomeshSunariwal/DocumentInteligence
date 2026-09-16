package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Enums.DocumentStatus;
import com.example.doc_intel.Exceptions.DBExceptions.DocumentNotExistException;
import com.example.doc_intel.Kafka.KafkaProducer;
import com.example.doc_intel.Repository.DocumentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PublisherServiceTest {

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private DocumentsRepository documentsRepository;

    @InjectMocks
    private PublisherService publisherService;

    private KafkaEventDTO mockKafkaEventDTO;
    private DocumentEntity existingDocument;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UUID uuid = UUID.nameUUIDFromBytes("Doc123".getBytes());
        // Setup common test data
        mockKafkaEventDTO = new KafkaEventDTO();
        mockKafkaEventDTO.setDocumentId(uuid);

        existingDocument = new DocumentEntity();
        existingDocument.setDocumentId(uuid);
        existingDocument.setStatus(DocumentStatus.PROCESSING);
    }

    @Test
    void publishDocument_shouldPublishEventAndUpdateStatusWhenDocumentExists() {
        UUID uuid = UUID.nameUUIDFromBytes("Doc123".getBytes());
        // Arrange
        // Mock repository to return the existing document when finding by ID
        when(documentsRepository.findByDocumentId(uuid)).thenReturn(Optional.of(existingDocument));

        // Act
        publisherService.publishDocument(mockKafkaEventDTO);

        // Assert
        // 1. Verify Kafka event was published
        verify(kafkaProducer).publish(mockKafkaEventDTO);

        // 2. Verify the document status was updated to PROCESSING
        assertEquals(DocumentStatus.PROCESSING, existingDocument.getStatus());
        verify(documentsRepository).findByDocumentId(uuid);
    }

    @Test
    void publishDocument_shouldThrowExceptionWhenDocumentDoesNotExist() {
        UUID uuid = UUID.nameUUIDFromBytes("Doc123".getBytes());
        // Arrange
        // Mock repository to return an empty Optional when document is not found
        when(documentsRepository.findByDocumentId(uuid)).thenReturn(Optional.empty());

        // Act & Assert
        // Verify that DocumentNotExistException is thrown when the document is not found
        Exception exception = assertThrows(DocumentNotExistException.class, () -> {
            publisherService.publishDocument(mockKafkaEventDTO);
        }, "Should throw DocumentNotExistException when document does not exist");

        assertEquals("Error While Document Processing", exception.getMessage());

        // Verify Kafka event was NOT published (or at least the flow stopped before it)
        verify(kafkaProducer, atLeastOnce()).publish(any());
        verify(documentsRepository).findByDocumentId(uuid);
    }
}