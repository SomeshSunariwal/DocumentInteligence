package com.example.doc_intel.Client;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Exceptions.OpenSearchException.OpenSearchIndexingException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@Lazy
public class OpenSearchClientProvider implements Client<OpenSearchClient> {

    private final OpenSearchClient openSearchClient;


    OpenSearchClientProvider(
            @Value("${OPEN.SEARCH.HOST}") String HOST,
            @Value("${OPEN.SEARCH.PORT}") Integer PORT,
            @Value("${OPEN.SEARCH.ROOT.USER}") String USER_NAME,
            @Value("${OPEN.SEARCH.ROOT.PASSWORD}") String PASSWORD
    ) {
        HttpHost host = new HttpHost("http", HOST, PORT);
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(host),
                new UsernamePasswordCredentials(USER_NAME, PASSWORD.toCharArray())
        );

        OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
                .builder(host)
                .setHttpClientConfigCallback(
                        httpClientBuilder ->
                                httpClientBuilder
                                        .setDefaultCredentialsProvider(credentialsProvider))
                .build();

        this.openSearchClient = new OpenSearchClient(transport);
    }

    public OpenSearchClient getClient() {
        return openSearchClient;
    }

    /***
     * To store the KNN type vector mapping else it would be float type
     */
    @PostConstruct
    public void initializeIndex() {
        try {
            boolean exists =
                    getClient()
                            .indices()
                            .exists(e -> e.index(Constants.OPEN_SEARCH_INDEX_NAME))
                            .value();
            if (exists) {
                log.info("OpenSearch index '{}' already exists", Constants.OPEN_SEARCH_INDEX_NAME);
                return;
            }
            log.info("Creating OpenSearch index '{}'", Constants.OPEN_SEARCH_INDEX_NAME);
            getClient()
                    .indices()
                    .create(c -> c
                            .index("pdf-documents")
                            .settings(s -> s.index(i -> i.knn(true)))
                            .mappings(m -> m
                                    .properties("vector", p -> p
                                            .knnVector(k -> k
                                                    .dimension(384)
                                                    .spaceType("cosinesimil")
                                            )
                                    )
                            )
                );

            log.info("OpenSearch index '{}' created successfully", Constants.OPEN_SEARCH_INDEX_NAME);
        } catch (IOException e) {
            throw new OpenSearchIndexingException("Failed to initialize OpenSearch index");
        }
    }
}