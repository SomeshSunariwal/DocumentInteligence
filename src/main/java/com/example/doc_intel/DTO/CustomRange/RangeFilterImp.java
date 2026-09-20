package com.example.doc_intel.DTO.CustomRange;

import dev.langchain4j.store.embedding.filter.Filter;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RangeFilterImp implements RangeFilter {

    private String key;

    private Long min;

    private Long max;

    @Override
    public String key() {
        return key;
    }

    @Override
    public Long min() {
        return min;
    }

    @Override
    public Long max() {
        return max;
    }

    @Override
    public boolean test(Object object) {
        /** TODO
         * Its needed to test something on local
         */

        // Not Needed Right Now
        return false;
    }

    @Override
    public Filter and(Filter filter) {
        return RangeFilter.super.and(filter);
    }

    @Override
    public Filter or(Filter filter) {
        return RangeFilter.super.or(filter);
    }
}
