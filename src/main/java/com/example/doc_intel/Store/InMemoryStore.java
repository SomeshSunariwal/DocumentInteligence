package com.example.doc_intel.Store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class InMemoryStore implements Store{

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(InMemoryStore.class);

    @Override
    public EmbeddingStore<TextSegment> giveMeStore() {
        log.info("Using In Memory Store");
        return new InMemoryEmbeddingStore<>();
    }
}
