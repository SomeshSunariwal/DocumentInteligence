package com.example.doc_intel.EmbedingStore;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;

public interface CustomEmbeddingMethods {

    EmbeddingSearchResult<TextSegment> searchByFilter(Filter filter);
}
