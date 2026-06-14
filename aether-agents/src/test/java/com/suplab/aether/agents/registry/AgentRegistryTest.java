package com.suplab.aether.agents.registry;

import com.suplab.aether.agents.spi.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRegistryTest {

    private AgentRegistry registry;

    @BeforeEach
    void setUp() {
        var gov = new StubAgent("GovernanceAgent", Set.of(AgentCapability.GOVERNANCE));
        var retry = new StubAgent("RetryAgent", Set.of(AgentCapability.RETRY_STRATEGY));
        registry = new AgentRegistry(List.of(gov, retry));
    }

    @Test
    void findByCapability_returnsMatchingAgents() {
        var agents = registry.findByCapability(AgentCapability.GOVERNANCE);
        assertThat(agents).hasSize(1);
        assertThat(agents.getFirst().agentType()).isEqualTo("GovernanceAgent");
    }

    @Test
    void findByCapability_returnsEmpty_forUnregisteredCapability() {
        assertThat(registry.findByCapability(AgentCapability.HALLUCINATION_DETECTION)).isEmpty();
    }

    @Test
    void disableAgent_removesFromDispatch() {
        registry.disableAgent("GovernanceAgent");
        assertThat(registry.findByCapability(AgentCapability.GOVERNANCE)).isEmpty();
    }

    @Test
    void enableAgent_restoresDispatch() {
        registry.disableAgent("GovernanceAgent");
        registry.enableAgent("GovernanceAgent");
        assertThat(registry.findByCapability(AgentCapability.GOVERNANCE)).hasSize(1);
    }

    @Test
    void agentCapabilityMap_containsAllAgents() {
        var map = registry.agentCapabilityMap();
        assertThat(map).containsKey("GovernanceAgent");
        assertThat(map).containsKey("RetryAgent");
        assertThat(map.get("GovernanceAgent")).contains(AgentCapability.GOVERNANCE);
    }

    private record StubAgent(String agentType, Set<AgentCapability> capabilities) implements Agent {
        @Override
        public AgentOutput execute(AgentInput input) {
            return new AgentOutput(input.callId(), agentType, AgentDecision.ALLOW,
                    0.9, false, "stub", Map.of(), null);
        }
    }
}
