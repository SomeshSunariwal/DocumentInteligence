package com.example.doc_intel.Client;

import com.example.doc_intel.Constants.Constants;
import com.example.doc_intel.Exceptions.OpenSearchIndexingException;
import jakarta.annotation.PostConstruct;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OpenSearchClientProvider {


    private static final Logger log = LoggerFactory.getLogger(OpenSearchClientProvider.class);

    public OpenSearchClient getOpenSearchClient() {
        HttpHost host = new HttpHost("http", "localhost", 9200);
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(host),
                new UsernamePasswordCredentials("admin", "DocIntel@1221".toCharArray())
        );

        OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
                .builder(host)
                .setHttpClientConfigCallback(
                        httpClientBuilder ->
                                httpClientBuilder
                                        .setDefaultCredentialsProvider(credentialsProvider))
                .build();

        return new OpenSearchClient(transport);
    }

    /***
     * To store the KNN type vector mapping else it would be float type
     */
    @PostConstruct
    public void initializeIndex() {
        try {
            boolean exists = getOpenSearchClient()
                    .indices()
                    .exists(e -> e.index(Constants.INDEX_NAME))
                    .value();
            if (exists) {
                log.info("OpenSearch index '{}' already exists", Constants.INDEX_NAME);
                return;
            }
            log.info("Creating OpenSearch index '{}'", Constants.INDEX_NAME);
            getOpenSearchClient()
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

            log.info("OpenSearch index '{}' created successfully", Constants.INDEX_NAME);
        } catch (IOException e) {
            throw new OpenSearchIndexingException("Failed to initialize OpenSearch index");
        }
    }
}