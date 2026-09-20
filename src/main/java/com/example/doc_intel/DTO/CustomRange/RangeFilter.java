package com.example.doc_intel.DTO.CustomRange;

import dev.langchain4j.store.embedding.filter.Filter;

public interface RangeFilter extends Filter {

    String key();

    Long min();

    Long max();
}
