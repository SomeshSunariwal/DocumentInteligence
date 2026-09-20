package com.example.doc_intel.EmbedingStore;

import com.example.doc_intel.Enums.StoreType;
import com.example.doc_intel.Store.StoreFactory;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingRequestHandler {

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final CustomEmbeddingMethods customEmbeddingStoreMethods;

    public EmbeddingRequestHandler(final StoreFactory storeFactory,
                                   final CustomEmbeddingMethods customEmbeddingStoreMethods,
                                   @Value("${vector.data.store}") final StoreType storeType) {
        this.embeddingStore = storeFactory.giveMeStore(storeType).giveMeStore();
        this.customEmbeddingStoreMethods = customEmbeddingStoreMethods;
    }

    public EmbeddingSearchResult<TextSegment> makeRequest(@NonNull String query,
                                                          @NonNull Filter filter,
                                                          @NonNull Integer maxResult) {
        // 2. Convert question into embedding
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 3. Search OpenSearch
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(maxResult)
            .minScore(0.5)
            .filter(filter)
            .build();

        return embeddingStore.search(request);
    }

    public EmbeddingSearchResult<TextSegment> makeFilterRequest(@NonNull Filter filter) {
        return customEmbeddingStoreMethods.searchByFilter(filter);
    }
}
