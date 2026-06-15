package com.suplab.aether.agents.llm;

import com.suplab.aether.core.exception.AetherException;

public final class LlmClientException extends AetherException {

    public LlmClientException(String message) {
        super(message);
    }

    public LlmClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
