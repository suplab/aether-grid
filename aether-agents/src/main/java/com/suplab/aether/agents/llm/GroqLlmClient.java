package com.suplab.aether.agents.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Groq API adapter — provides access to Llama, Mixtral, Gemma and other HuggingFace-origin
 * models via Groq's OpenAI-compatible Chat Completions API.
 *
 * Required env var: GROQ_API_KEY
 */
public class GroqLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(GroqLlmClient.class);
    private static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1";

    private final RestClient restClient;
    private final String defaultModel;

    public GroqLlmClient(String apiKey, String defaultModel) {
        this.restClient = RestClient.builder()
                .baseUrl(GROQ_BASE_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.defaultModel = defaultModel;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        var messages = new ArrayList<ChatMessage>();
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            messages.add(new ChatMessage("system", request.systemPrompt()));
        }
        messages.add(new ChatMessage("user", request.userPrompt()));

        var body = new ChatCompletionRequest(
                request.model().isBlank() ? defaultModel : request.model(),
                messages,
                request.temperature(),
                request.maxTokens()
        );

        var start = Instant.now();
        try {
            var response = restClient.post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new LlmClientException("Groq returned empty response");
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            var content = response.choices().getFirst().message().content();
            var usage = response.usage();

            return new LlmResponse(
                    content,
                    body.model(),
                    LlmProvider.GROQ,
                    usage != null ? usage.promptTokens() : 0,
                    usage != null ? usage.completionTokens() : 0,
                    latencyMs
            );
        } catch (RestClientException e) {
            throw new LlmClientException("Groq request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public LlmProvider provider() {
        return LlmProvider.GROQ;
    }

    @Override
    public boolean isAvailable() {
        try {
            restClient.get().uri("/models").retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Groq health check failed: {}", e.getMessage());
            return false;
        }
    }

    record ChatMessage(String role, String content) {}

    record ChatCompletionRequest(
            String model,
            List<ChatMessage> messages,
            double temperature,
            @JsonProperty("max_tokens") int maxTokens
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatCompletionResponse(List<Choice> choices, Usage usage) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Choice(ChatMessage message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Usage(
            @JsonProperty("prompt_tokens") int promptTokens,
            @JsonProperty("completion_tokens") int completionTokens
    ) {}
}
