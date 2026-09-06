package com.example.doc_intel.Store;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
public class OpenSearchStore implements Store{
    @Override
    public EmbeddingStore<TextSegment> giveMeStore() {
        return OpenSearchEmbeddingStore
                .builder()
                .serverUrl("http://localhost:9200")
                .userName("admin")
                .password("DocIntel@1221")
                .indexName("my-embeddings")
                .build();
    }
}
