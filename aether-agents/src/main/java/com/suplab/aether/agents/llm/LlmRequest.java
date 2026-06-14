package com.suplab.aether.agents.llm;

import java.util.Map;

public record LlmRequest(
        String model,
        String systemPrompt,
        String userPrompt,
        double temperature,
        int maxTokens,
        Map<String, Object> metadata
) {
    public LlmRequest {
        if (model == null || model.isBlank()) throw new IllegalArgumentException("model must not be blank");
        if (userPrompt == null || userPrompt.isBlank()) throw new IllegalArgumentException("userPrompt must not be blank");
        if (temperature < 0.0 || temperature > 2.0) throw new IllegalArgumentException("temperature must be between 0.0 and 2.0");
        if (maxTokens <= 0) throw new IllegalArgumentException("maxTokens must be positive");
        if (metadata == null) metadata = Map.of();
    }

    public static LlmRequest of(String model, String systemPrompt, String userPrompt) {
        return new LlmRequest(model, systemPrompt, userPrompt, 0.7, 1024, Map.of());
    }
}
