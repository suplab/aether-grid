package com.suplab.aether.agents.llm;

public interface LlmClient {

    LlmResponse complete(LlmRequest request);

    LlmProvider provider();

    boolean isAvailable();
}
