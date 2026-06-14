package com.suplab.aether.agents.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LlmClientConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "aether.llm.provider", havingValue = "ollama", matchIfMissing = true)
    public LlmClient ollamaLlmClient(
            @Value("${aether.llm.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${aether.llm.ollama.model:gemma2:2b}") String model
    ) {
        return new OllamaLlmClient(baseUrl, model);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "aether.llm.provider", havingValue = "groq")
    public LlmClient groqLlmClient(
            @Value("${aether.llm.groq.api-key:${GROQ_API_KEY:}}") String apiKey,
            @Value("${aether.llm.groq.model:llama-3.3-70b-versatile}") String model
    ) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY must be set when aether.llm.provider=groq");
        }
        return new GroqLlmClient(apiKey, model);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "aether.llm.provider", havingValue = "anthropic")
    public LlmClient anthropicLlmClient(
            @Value("${aether.llm.anthropic.api-key:${ANTHROPIC_API_KEY:}}") String apiKey,
            @Value("${aether.llm.anthropic.model:claude-haiku-4-5-20251001}") String model
    ) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY must be set when aether.llm.provider=anthropic");
        }
        return new AnthropicLlmClient(apiKey, model);
    }

    @Bean
    @ConditionalOnMissingBean(LlmClient.class)
    public LlmClient fallbackLlmClient(
            @Value("${aether.llm.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${aether.llm.ollama.model:gemma2:2b}") String model
    ) {
        return new OllamaLlmClient(baseUrl, model);
    }
}
