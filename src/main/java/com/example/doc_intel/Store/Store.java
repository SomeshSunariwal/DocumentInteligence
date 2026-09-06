package com.example.doc_intel.Store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Component;

@Component
public interface Store {
    EmbeddingStore<TextSegment> giveMeStore();
}
