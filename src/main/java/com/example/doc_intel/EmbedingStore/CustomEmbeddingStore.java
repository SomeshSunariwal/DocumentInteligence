package com.example.doc_intel.EmbedingStore;

import com.example.doc_intel.Client.OpenSearchClientProvider;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Exceptions.OpenSearchIndexingException;
import com.example.doc_intel.Exceptions.OpenSearchVectoreException;
import com.example.doc_intel.Utils.Utils;
import com.example.doc_intel.DTO.EmbeddingDocument;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import dev.langchain4j.data.document.Metadata;
import org.opensearch.client.opensearch._types.query_dsl.KnnQuery;

import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.UUID;
import java.util.HashMap;

@Component
@Lazy
@Slf4j
public class CustomEmbeddingStore implements EmbeddingStore<TextSegment> {

    private final OpenSearchClientProvider client;

    public CustomEmbeddingStore(OpenSearchClientProvider client) {
        this.client = client;
    }

    @Override
    public String add(Embedding embedding) {
        String id = UUID.randomUUID().toString();
        add(id, embedding);
        return id;
    }

    @Override
    public void add(String id, Embedding embedding) {
        indexDocument(id, embedding, null);
    }

    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        String id = UUID.randomUUID().toString();
        indexDocument(id, embedding, textSegment);
        return id;
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        List<String> ids = new ArrayList<>(embeddings.size());
        for (Embedding embedding : embeddings) {
            ids.add(add(embedding));
        }
        return ids;
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

        try {
            log.info("Search Started...");
            List<Float> queryVector = request.queryEmbedding().vectorAsList();
            int maxResults = request.maxResults();

            KnnQuery knnQuery = new KnnQuery.Builder()
                    .field("vector")
                    .vector(queryVector)
                    .k(maxResults)
                    .build();

            Query query = new Query.Builder()
                    .knn(knnQuery)
                    .build();

            // 3. Execute search
            SearchResponse<EmbeddingDocument> response =
                    client.getOpenSearchClient().search(new SearchRequest.Builder()
                                    .index(Constants.INDEX_NAME)
                                    .size(maxResults)
                                    .query(query)
                                    .build(),
                            EmbeddingDocument.class);

            for (Hit<EmbeddingDocument> hit : response.hits().hits()) {
                EmbeddingDocument source = hit.source();
                if (source == null) {
                    continue;
                }
                Embedding embedding = Embedding.from(source.getVector());
                String text = source.getText();
                Metadata metadata = Utils.convertToMetaData(source.getMetadata());
                TextSegment textSegment = TextSegment.from(text, metadata);

                double score = hit.score() == null ? 0.0 : hit.score();
                if (score < request.minScore()) {
                    continue;
                }
                EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(score, hit.id(), embedding, textSegment);
                matches.add(match);
            }

        } catch (IOException e) {
            log.error("Failed -", e);
            throw new OpenSearchVectoreException("OpenSearch vector search failed");
        } catch (Exception e) {
            log.error("Failed -> ", e);
        }
        return new EmbeddingSearchResult<>(matches);
    }

    private void indexDocument(String id, Embedding embedding, TextSegment textSegment) {
        try {
            Map<String, Object> document = new HashMap<>();
            document.put("vector", embedding.vectorAsList());

            if (textSegment != null) {
                document.put("text", textSegment.text());
                document.put("metadata", textSegment.metadata().toMap());
            }
            client.getOpenSearchClient().index(
                    i -> i
                            .id(id)
                            .index(Constants.INDEX_NAME)
                            .document(document));
        } catch (IOException e) {
            throw new OpenSearchIndexingException("Failed to index embedding");
        }
    }
}