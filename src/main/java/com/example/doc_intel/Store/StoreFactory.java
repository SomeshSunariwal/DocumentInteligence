package com.example.doc_intel.Store;

import com.example.doc_intel.Enums.StoreType;
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

    public Store giveMeStore (StoreType storeType) {
        if (StoreType.OPEN_STORE.equals(storeType)) {
            return openSearchStore;
        } else if (StoreType.IN_MEMORY.equals(storeType)) {
            return inMemoryStore;
        }
        return inMemoryStore;
    }
}
