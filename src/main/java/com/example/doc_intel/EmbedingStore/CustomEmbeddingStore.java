package com.example.doc_intel.EmbedingStore;

import com.example.doc_intel.Client.OpenSearchClientProvider;
import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.DTO.CustomRange.RangeFilter;
import com.example.doc_intel.Exceptions.InternalServerErrorException;
import com.example.doc_intel.Exceptions.OpenSearchException.OpenSearchIndexingException;
import com.example.doc_intel.Exceptions.OpenSearchException.OpenSearchVectoreException;
import com.example.doc_intel.Exceptions.OpenSearchException.UnsupportedFilterException;
import com.example.doc_intel.Utils.Utils;
import com.example.doc_intel.DTO.EmbeddingDocument;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.logical.And;
import dev.langchain4j.store.embedding.filter.logical.Or;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
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
@Slf4j
public class CustomEmbeddingStore implements EmbeddingStore<TextSegment>, CustomEmbeddingMethods {

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

            Query userFilter = buildFilterQuery(request.filter());

            KnnQuery knnQuery = new KnnQuery.Builder()
                .field("vector")
                .vector(queryVector)
                .k(maxResults)
                .filter(userFilter)
                .build();

            Query query = new Query.Builder()
                .knn(knnQuery)
                .build();

            // 3. Execute search
            SearchResponse<EmbeddingDocument> response =
                client.getClient().search(new SearchRequest.Builder()
                        .index(Constants.OPEN_SEARCH_INDEX_NAME)
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
            log.error("OpenSearch vector search failed", e);
            throw new OpenSearchVectoreException("OpenSearch vector search failed");
        } catch (Exception e) {
            log.error("OpenSearch failed", e);
            throw new InternalServerErrorException("OpenSearch failed");
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
            client.getClient().index(
                i -> i
                    .id(id)
                    .index(Constants.OPEN_SEARCH_INDEX_NAME)
                    .document(document));
        } catch (IOException e) {
            throw new OpenSearchIndexingException("Failed to index embedding");
        }
    }

    @Override
    public EmbeddingSearchResult<TextSegment> searchByFilter(Filter filter) {

        try {
            Query userFilter = buildFilterQuery(filter);
            SearchResponse<EmbeddingDocument> response =
                client.getClient().search(
                    new SearchRequest.Builder()
                        .index(Constants.OPEN_SEARCH_INDEX_NAME)
                        .size(100)
                        .query(userFilter)
                        .build(),
                    EmbeddingDocument.class
                );

            List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
            for (Hit<EmbeddingDocument> hit : response.hits().hits()) {

                EmbeddingDocument source = hit.source();
                if (source == null) {
                    continue;
                }
                log.info("Hit Id : {}", hit.id());
                Embedding embedding = Embedding.from(source.getVector());
                String text = source.getText();
                Metadata metadata = Utils.convertToMetaData(source.getMetadata());
                TextSegment textSegment = TextSegment.from(text, metadata);
                double score = hit.score() == null ? 0.0 : hit.score();
                matches.add(new EmbeddingMatch<>(score, hit.id(), embedding, textSegment));
            }
            return new EmbeddingSearchResult<>(matches);
        } catch (IOException e) {
            log.error("OpenSearch filter search failed", e);
            throw new OpenSearchVectoreException("OpenSearch filter search failed");
        } catch (Exception e) {
            log.error("OpenSearch failed", e);
            throw new InternalServerErrorException("OpenSearch failed");
        }
    }

    private Query buildFilterQuery(Filter filter) {
        if (filter instanceof IsEqualTo equalTo) {
            String field = Utils.getFilterField(equalTo.key());
            Object value = equalTo.comparisonValue();

            if (value instanceof Number number) {
                return new Query.Builder()
                    .term(t -> t
                        .field(field)
                        .value(FieldValue.of(number.longValue())))
                    .build();
            }

            return new Query.Builder()
                .term(t -> t
                    .field(field)
                    .value(FieldValue.of(value.toString())))
                .build();
        }

        if (filter instanceof RangeFilter range) {
            return new Query.Builder()
                .range(r -> {
                    r.field("metadata." + range.key());
                    if (range.min() != null) r.gte(JsonData.of(range.min()));
                    if (range.max() != null) r.lte(JsonData.of(range.max()));
                    return r;
                })
                .build();
        }

        if (filter instanceof And and) {
            Query leftQuery = buildFilterQuery(and.left());
            Query rightQuery = buildFilterQuery(and.right());
            return new Query.Builder()
                .bool(b -> b.must(leftQuery, rightQuery))
                .build();
        }

        if (filter instanceof Or or) {
            Query leftQuery = buildFilterQuery(or.left());
            Query rightQuery = buildFilterQuery(or.right());
            return new Query.Builder()
                .bool(b -> b.should(leftQuery, rightQuery)
                    .minimumShouldMatch("1")
                )
                .build();
        }

        throw new UnsupportedFilterException("Internal Server Error");
    }
}