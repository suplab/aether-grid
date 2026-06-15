package com.suplab.aether.memory.embedding;

import com.suplab.aether.core.exception.AetherException;

public final class EmbeddingException extends AetherException {

    public EmbeddingException(String message) {
        super(message);
    }

    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}
