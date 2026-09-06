package com.example.doc_intel.Store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Component;

@Component
public class InMemoryStore implements Store{
    @Override
    public EmbeddingStore<TextSegment> giveMeStore() {
        return new InMemoryEmbeddingStore<>();
    }
}
