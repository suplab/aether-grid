package com.suplab.aether.core.exception;

public abstract class AetherException extends RuntimeException {

    protected AetherException(String message) {
        super(message);
    }

    protected AetherException(String message, Throwable cause) {
        super(message, cause);
    }
}
