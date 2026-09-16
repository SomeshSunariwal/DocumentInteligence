package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.EncoderModel;
import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.DocumentEncoder.DocumentEncoder;
import com.example.doc_intel.DocumentEncoder.DocumentEncoderFactory;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Enums.DocumentStatus;
import com.example.doc_intel.Enums.FileExtensions;
import com.example.doc_intel.Enums.StoreType;
import com.example.doc_intel.Exceptions.DBExceptions.DocumentNotExistException;
import com.example.doc_intel.Exceptions.ProcessFileException;
import com.example.doc_intel.MinIOProcesser.MinIOProcessor;
import com.example.doc_intel.Repository.DocumentsRepository;
import com.example.doc_intel.Store.StoreFactory;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DocumentProcessorService {

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final DocumentEncoderFactory documentEncoderFactory;

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    private DocumentEncoder documentEncoder;

    private final DocumentsRepository documentsRepository;

    private final MinIOProcessor minIOProcessor;

    DocumentProcessorService(@Value("${vector.data.store}") final StoreType storeType,
                             final StoreFactory storeFactory,
                             final DocumentEncoderFactory documentEncoderFactory,
                             final DocumentsRepository documentsRepository,
                             final MinIOProcessor minIOProcessor
    ) {
        this.embeddingStore = storeFactory.giveMeStore(storeType).giveMeStore();
        this.documentEncoderFactory = documentEncoderFactory;
        this.documentsRepository = documentsRepository;
        this.minIOProcessor = minIOProcessor;
    }

    @Transactional
    public void processDocumentFromKafka(@NonNull KafkaEventDTO event) {
        //---------------------- Document Encoding and Storing Embedding

        Optional<DocumentEntity> optionalDocumentEntity = documentsRepository.findByDocumentId(event.getDocumentId());
        if (optionalDocumentEntity.isEmpty()) {
            throw new DocumentNotExistException("Document Not Found During Kafka Process");
        }
        DocumentEntity documentEntity = optionalDocumentEntity.get();

        // GetFile from the MinIO
        InputStream file = minIOProcessor.getObject(documentEntity.getObjectKey(), event.getVersionId());

        // Store Embeddings
        try {
            StoreEmbeddings(file, documentEntity.getFileExtensions(),
                event.getUserId().toString(),
                documentEntity.getVersion(),
                event.getDocumentId().toString(),
                documentEntity.getFileName()
            );
        } catch (RuntimeException e) {
            log.error("Failed to process the event: {}", event.getEventId());
            documentEntity.setStatus(DocumentStatus.FAILED);
        }

        // Update Database
        documentEntity.setStatus(DocumentStatus.COMPLETED);
    }

    /**
     * Store Embeddings in the Vector Store
     */
    private void StoreEmbeddings(@NonNull InputStream fileStream, @NonNull FileExtensions fileExtension,
                                 @NonNull String userId, @NonNull Integer version, @NonNull String documentId,
                                 @NonNull String fileName) {
        documentEncoder = documentEncoderFactory.getParser(fileExtension);
        List<TextSegment> chunks;
        try {
            // Created Encoder Model
            EncoderModel encoderModel = EncoderModel.builder()
                .fileStream(fileStream)
                .userId(userId)
                .version(version)
                .documentId(documentId)
                .fileName(fileName).build();

            chunks = documentEncoder.encode(encoderModel);
            log.info("Number of chunks: {}", chunks.size());
            for (TextSegment chunk : chunks) {
                Embedding embedding = embeddingModel.embed(chunk).content();
                embeddingStore.add(embedding, chunk);
            }
        } catch (Exception e) {
            throw new ProcessFileException("Internal Server Error");
        }
    }
}
