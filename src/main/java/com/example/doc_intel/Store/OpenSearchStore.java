package com.example.doc_intel.Store;

import com.example.doc_intel.Client.OpenSearchClientProvider;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.EmbedingStore.CustomEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OpenSearchStore implements Store{

    private static final Logger log = LoggerFactory.getLogger(OpenSearchStore.class);

    private final OpenSearchClientProvider client;

    public OpenSearchStore(OpenSearchClientProvider client) {
        this.client = client;
    }

    @Override
    public EmbeddingStore<TextSegment> giveMeStore() {
        log.info("Using Open Search Store");
        return new CustomEmbeddingStore(client);
    }
}
