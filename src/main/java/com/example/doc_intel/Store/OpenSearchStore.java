package com.example.doc_intel.Store;

import com.example.doc_intel.Client.OpenSearchClientProvider;
import com.example.doc_intel.EmbedingStore.CustomEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Lazy
public class OpenSearchStore implements Store{

    private final OpenSearchClientProvider client;

    public OpenSearchStore(@Lazy OpenSearchClientProvider client) {
        this.client = client;
    }

    @Override
    public EmbeddingStore<TextSegment> giveMeStore() {
        log.info("Using Open Search Store");
        return new CustomEmbeddingStore(client);
    }
}
