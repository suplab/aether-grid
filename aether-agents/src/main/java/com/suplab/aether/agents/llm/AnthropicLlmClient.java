package com.suplab.aether.agents.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Anthropic Claude API adapter — provides access to claude-haiku-4-5, claude-sonnet-4-6,
 * and other Claude models via Anthropic's Messages API.
 *
 * Required env var: ANTHROPIC_API_KEY
 */
public class AnthropicLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicLlmClient.class);
    private static final String ANTHROPIC_BASE_URL = "https://api.anthropic.com/v1";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final RestClient restClient;
    private final String defaultModel;

    public AnthropicLlmClient(String apiKey, String defaultModel) {
        this.restClient = RestClient.builder()
                .baseUrl(ANTHROPIC_BASE_URL)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", ANTHROPIC_VERSION)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.defaultModel = defaultModel;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        var model = request.model().isBlank() ? defaultModel : request.model();
        var body = new MessagesRequest(
                model,
                request.maxTokens(),
                request.systemPrompt(),
                List.of(new Message("user", request.userPrompt())),
                request.temperature()
        );

        var start = Instant.now();
        try {
            var response = restClient.post()
                    .uri("/messages")
                    .body(body)
                    .retrieve()
                    .body(MessagesResponse.class);

            if (response == null || response.content() == null || response.content().isEmpty()) {
                throw new LlmClientException("Anthropic returned empty response");
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            var content = response.content().getFirst().text();
            var usage = response.usage();

            return new LlmResponse(
                    content,
                    model,
                    LlmProvider.ANTHROPIC,
                    usage != null ? usage.inputTokens() : 0,
                    usage != null ? usage.outputTokens() : 0,
                    latencyMs
            );
        } catch (RestClientException e) {
            throw new LlmClientException("Anthropic request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public LlmProvider provider() {
        return LlmProvider.ANTHROPIC;
    }

    @Override
    public boolean isAvailable() {
        try {
            restClient.get().uri("/models").retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Anthropic health check failed: {}", e.getMessage());
            return false;
        }
    }

    record MessagesRequest(
            String model,
            @JsonProperty("max_tokens") int maxTokens,
            String system,
            List<Message> messages,
            double temperature
    ) {}

    record Message(String role, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MessagesResponse(List<ContentBlock> content, Usage usage) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ContentBlock(String type, String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Usage(
            @JsonProperty("input_tokens") int inputTokens,
            @JsonProperty("output_tokens") int outputTokens
    ) {}
}
