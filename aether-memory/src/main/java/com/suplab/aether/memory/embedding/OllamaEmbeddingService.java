package com.suplab.aether.memory.embedding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.suplab.aether.core.ports.EmbeddingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

public class OllamaEmbeddingService implements EmbeddingPort {

    private static final Logger log = LoggerFactory.getLogger(OllamaEmbeddingService.class);

    private final RestClient restClient;
    private final String model;

    public OllamaEmbeddingService(String baseUrl, String model) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.model = model;
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Cannot embed blank text");
        }
        try {
            var response = restClient.post()
                    .uri("/api/embed")
                    .body(new EmbedRequest(model, text))
                    .retrieve()
                    .body(EmbedResponse.class);

            if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
                throw new EmbeddingException("Ollama returned empty embedding for model " + model);
            }

            var embedding = response.embeddings().getFirst();
            if (embedding.length != 384) {
                log.warn("Unexpected embedding dimension {} from model {} (expected 384)", embedding.length, model);
            }
            return embedding;

        } catch (RestClientException e) {
            throw new EmbeddingException("Ollama embedding request failed: " + e.getMessage(), e);
        }
    }

    record EmbedRequest(String model, String input) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record EmbedResponse(List<float[]> embeddings) {}
}
