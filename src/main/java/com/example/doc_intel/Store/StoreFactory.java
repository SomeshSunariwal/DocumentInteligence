package com.example.doc_intel.Store;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StoreFactory {

    private final OpenSearchStore openSearchStore ;

    private final InMemoryStore inMemoryStore ;

    public StoreFactory (OpenSearchStore openSearchStore, InMemoryStore inMemoryStore) {
        this.inMemoryStore = inMemoryStore;
        this.openSearchStore = openSearchStore;
    }

    public Store giveMeStore (String name) {
        if (Objects.equals(name, "openSearch")) {
            return openSearchStore;
        } else if (Objects.equals(name, "inMemory")) {
            return inMemoryStore;
        }
        return inMemoryStore;
    }
}
