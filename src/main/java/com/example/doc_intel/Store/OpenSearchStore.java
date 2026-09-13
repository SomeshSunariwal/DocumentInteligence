package com.example.doc_intel.Store;

import com.example.doc_intel.EmbedingStore.CustomEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OpenSearchStore implements Store {

    private final CustomEmbeddingStore customEmbeddingStore;

    public OpenSearchStore(CustomEmbeddingStore customEmbeddingStore) {
        this.customEmbeddingStore = customEmbeddingStore;
    }

    @Override
    public EmbeddingStore<TextSegment> giveMeStore() {
        log.info("Using Open Search Store");
        return customEmbeddingStore;
    }
}
