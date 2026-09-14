package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.DocumentsDTO.DocumentUploadResponseDTO;
import com.example.doc_intel.DTO.FileRequestDTO;
import com.example.doc_intel.DocumentEncoder.DocumentEncoder;
import com.example.doc_intel.DocumentEncoder.DocumentEncoderFactory;
import com.example.doc_intel.Enums.StoreType;
import com.example.doc_intel.Exceptions.ProcessFileException;
import com.example.doc_intel.Exceptions.MessageLengthException;
import com.example.doc_intel.Exceptions.UnSupportedFileException;
import com.example.doc_intel.LongChainChatModel.ChatModelFactory;
import com.example.doc_intel.Store.StoreFactory;
import com.example.doc_intel.Utils.Utils;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class DocumentProcessService {

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    private final DocumentEncoderFactory documentEncoderFactory;

    private DocumentEncoder documentEncoder;

    DocumentProcessService(StoreFactory storeFactory,
                           ChatModelFactory chatModelFactory,
                           DocumentEncoderFactory documentEncoderFactory,
                           @Value("${vector.data.store}") final StoreType storeType) {
        this.embeddingStore = storeFactory.giveMeStore(storeType).giveMeStore();
        this.documentEncoderFactory = documentEncoderFactory;
    }

    public DocumentUploadResponseDTO processDocument(@NonNull MultipartFile file) {
        List<TextSegment> chunks;
        String extension = Utils.getExtension(file);
        if (Objects.isNull(extension)) {
            throw new UnSupportedFileException("File Type not support");
        }
        documentEncoder = documentEncoderFactory.getParser(extension);

        try {
            chunks = documentEncoder.encode(file);
        } catch (Exception e) {
            throw new ProcessFileException("Internal Server Error");
        }

        log.info("Number of chunks: {}", chunks.size());
        for (TextSegment chunk : chunks) {
            Embedding embedding = embeddingModel.embed(chunk).content();
            embeddingStore.add(embedding, chunk);
        }
        log.info("All Chunk Stored Successfully");
        return new DocumentUploadResponseDTO("Document processed successfully.", "1");
    }

    public DocumentUploadResponseDTO processFile(FileRequestDTO fileRequestDTO) {

        String[] message = fileRequestDTO.getMessage();
        if (message.length <= 0) {
            throw new MessageLengthException("Message Length is 0");
        }
        String text = String.join(",", message);

        try {
            Document document = Document.from(text);
            DocumentSplitter splitter = DocumentSplitters.recursive(100, 50);
            List<TextSegment> chunks = splitter.split(document);
            log.info("Number of chunks: {}", chunks.size());

            for (TextSegment chunk : chunks) {
                Embedding embedding = embeddingModel.embed(chunk).content();
                String id = embeddingStore.add(embedding, chunk);
                log.info("Stored chunk: {}", id);
            }
            return new DocumentUploadResponseDTO("Document processed successfully.", "1");
        } catch (Exception e) {
            throw new ProcessFileException("Internal Server Error");
        }
    }
}
