package com.suplab.aether.agents.spi;

import java.util.Set;

public interface Agent {

    String agentType();

    Set<AgentCapability> capabilities();

    AgentOutput execute(AgentInput input);

    default boolean supports(AgentCapability capability) {
        return capabilities().contains(capability);
    }

    default boolean isEnabled() {
        return true;
    }
}
