package com.suplab.aether.agents.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class OllamaLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaLlmClient.class);

    private final RestClient restClient;
    private final String defaultModel;

    public OllamaLlmClient(String baseUrl, String defaultModel) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.defaultModel = defaultModel;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        var body = new OllamaRequest(
                request.model().isBlank() ? defaultModel : request.model(),
                buildMessages(request),
                false
        );

        var start = Instant.now();
        try {
            var response = restClient.post()
                    .uri("/api/chat")
                    .body(body)
                    .retrieve()
                    .body(OllamaResponse.class);

            if (response == null) throw new LlmClientException("Ollama returned null response");

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            return new LlmResponse(
                    response.message() != null ? response.message().content() : "",
                    body.model(),
                    LlmProvider.OLLAMA,
                    response.promptEvalCount(),
                    response.evalCount(),
                    latencyMs
            );
        } catch (RestClientException e) {
            throw new LlmClientException("Ollama request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public LlmProvider provider() {
        return LlmProvider.OLLAMA;
    }

    @Override
    public boolean isAvailable() {
        try {
            restClient.get().uri("/api/tags").retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Ollama health check failed: {}", e.getMessage());
            return false;
        }
    }

    private List<OllamaMessage> buildMessages(LlmRequest request) {
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            return List.of(
                    new OllamaMessage("system", request.systemPrompt()),
                    new OllamaMessage("user", request.userPrompt())
            );
        }
        return List.of(new OllamaMessage("user", request.userPrompt()));
    }

    record OllamaRequest(String model, List<OllamaMessage> messages, boolean stream) {}
    record OllamaMessage(String role, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OllamaResponse(
            OllamaMessage message,
            int promptEvalCount,
            int evalCount
    ) {}
}
