package com.example.doc_intel.Service;

import com.example.doc_intel.Exceptions.*;
import com.example.doc_intel.LongChainChatModel.ChatModelFactory;
import com.example.doc_intel.Reader.Reader;
import com.example.doc_intel.Reader.ReaderFactory;
import com.example.doc_intel.Store.StoreFactory;
import com.example.doc_intel.dto.*;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DocumentProcessService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatModel model;
    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    List<String> supportedTypes = Arrays.asList("pdf", "txt");
    private ReaderFactory readerFactory;
    private Reader reader;

    DocumentProcessService(StoreFactory storeFactory, ChatModelFactory chatModelFactory, ReaderFactory readerFactory) {
        this.embeddingStore = storeFactory.giveMeStore("inMemory").giveMeStore();
        this.model = chatModelFactory.giveMeChatModel("local").giveMeModel();
        this.readerFactory = readerFactory;

    }

    public DocumentProcessResponseDTO processDocument(MultipartFile file) {
        List<TextSegment> chunks;
        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            throw new FileSupportError("Unsupported File Format");
        }

        String extension = filename
                .substring(filename.lastIndexOf('.') + 1)
                .toLowerCase();

        if (!supportedTypes.contains(extension)) {
            throw new FileSupportError("Unsupported File Format");
        }

        reader = readerFactory.getDataFromReader(extension);

        try {
            final String text = reader.getParseFileData(file);
            Document document = Document.from(text);
            DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
            chunks = splitter.split(document);
        } catch (Exception e) {
            throw new ProcessFileException("Internal Server Error");
        }
        System.out.println("Number of chunks: " + chunks.size());
        for (TextSegment chunk : chunks) {
            Embedding embedding = embeddingModel.embed(chunk).content();
            String id = embeddingStore.add(embedding, chunk);
            System.out.println("Stored chunk: " + id);
        }
        return new DocumentProcessResponseDTO("Document processed successfully.", chunks.size());
    }

    public DocumentProcessResponseDTO processFile(FileRequestDTO fileRequestDTO) {

        String[] message = fileRequestDTO.getMessage();
        if (message.length <= 0) {
            throw new MessageLengthException("Message Length is 0");
        }
        String text = String.join(",", message);

        try {
            Document document = Document.from(text);
            DocumentSplitter splitter = DocumentSplitters.recursive(100, 50);
            List<TextSegment> chunks = splitter.split(document);
            System.out.println("Number of chunks: " + chunks.size());

            for (TextSegment chunk : chunks) {
                Embedding embedding = embeddingModel.embed(chunk).content();
                String id = embeddingStore.add(embedding, chunk);
                System.out.println("Stored chunk: " + id);
            }
            return new DocumentProcessResponseDTO("Document processed successfully.", chunks.size());
        } catch (Exception e) {
            throw new ProcessFileException("Internal Server Error");
        }
    }

    public QuestionResponseDTO processQuestion(QuestionRequestDTO questionDTO) {

        String message = questionDTO.getQuestion();
        if (Objects.isNull(message)) {
            throw new NullMessageException("Message cannot be null");
        }
        if (message.isEmpty()) {
            throw new MessageLengthException("Please Provide a Question");
        }
        // 2. Convert question into embedding
        Embedding queryEmbedding = embeddingModel.embed(questionDTO.getQuestion()).content();

        // 3. Search OpenSearch
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(5)
                .minScore(0.5)
                .build();


        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(request);

        // 4. Get relevant chunks
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
        System.out.println("Retrieved chunks: " + matches.size());

        // 5. Build context
        String context = matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        // 6. Create RAG prompt
        String prompt = """
                You are a document question-answering assistant.
                
                Answer the question using ONLY the context
                provided below.
                
                If the answer is not present in the context,
                say that you do not know.
                
                CONTEXT:
                %s
                
                QUESTION:
                %s
                
                ANSWER:
                """.formatted(context, questionDTO.getQuestion());

        try {
//            EmbeddingMatch<TextSegment> embeddingMatch = matches.getFirst();
            // 7. Send context + question to Local LLM
            String answer = model.chat(prompt);
            return new QuestionResponseDTO(answer, 100.0);
        } catch (NoSuchElementException e) {
            throw new NoResultFoundException("No Result Found, Make Sure Data is Already Fed");
        } catch (Exception e) {
            throw new ProcessFileException("Internal Server Error");
        }
    }
}
