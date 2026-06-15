package com.suplab.aether.core.exception;

public final class AgentException extends AetherException {

    private final String agentType;

    public AgentException(String agentType, String message) {
        super("Agent [" + agentType + "] failed: " + message);
        this.agentType = agentType;
    }

    public AgentException(String agentType, String message, Throwable cause) {
        super("Agent [" + agentType + "] failed: " + message, cause);
        this.agentType = agentType;
    }

    public String agentType() { return agentType; }
}
