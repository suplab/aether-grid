package com.suplab.aether.agents.llm;

public record LlmResponse(
        String content,
        String model,
        LlmProvider provider,
        int promptTokens,
        int completionTokens,
        long latencyMs
) {
    public LlmResponse {
        if (content == null) throw new IllegalArgumentException("content must not be null");
        if (model == null || model.isBlank()) throw new IllegalArgumentException("model must not be blank");
        if (provider == null) throw new IllegalArgumentException("provider must not be null");
    }

    public int totalTokens() {
        return promptTokens + completionTokens;
    }
}
