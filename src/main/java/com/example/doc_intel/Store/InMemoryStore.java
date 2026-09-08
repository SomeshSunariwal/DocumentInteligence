package com.example.doc_intel.Store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryStore implements Store {

    @Override
    public EmbeddingStore<TextSegment> giveMeStore() {
        log.info("Using In Memory Store");
        return new InMemoryEmbeddingStore<>();
    }
}
