package com.suplab.aether.agents.orchestrator;

import com.suplab.aether.agents.registry.AgentRegistry;
import com.suplab.aether.agents.spi.*;
import com.suplab.aether.core.domain.ApiCallId;
import com.suplab.aether.core.domain.TenantId;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentOrchestratorTest {

    private AgentOrchestrator orchestrator;

    private static final ApiCallId CALL_ID = ApiCallId.generate();
    private static final TenantId TENANT_ID = TenantId.generate();

    @BeforeEach
    void setUp() {
        var allowingAgent = new StubAgent("AllowAgent", AgentCapability.GOVERNANCE,
                AgentDecision.ALLOW, 0.9, "all clear");
        var registry = new AgentRegistry(List.of(allowingAgent));
        orchestrator = new AgentOrchestrator(registry, new SimpleMeterRegistry());
    }

    @Test
    void orchestrate_dispatches_to_matching_agent() {
        var input = new AgentInput(CALL_ID, TENANT_ID, AgentCapability.GOVERNANCE,
                "GET /v1/data 200 45ms", List.of(), Map.of());

        var result = orchestrator.orchestrate(input);

        assertThat(result.agentsFound()).isTrue();
        assertThat(result.outputs()).hasSize(1);
        assertThat(result.outputs().getFirst().decision()).isEqualTo(AgentDecision.ALLOW);
    }

    @Test
    void orchestrate_returns_noAgents_when_no_capability_match() {
        var input = new AgentInput(CALL_ID, TENANT_ID, AgentCapability.RETRY_STRATEGY,
                "POST /v1/data 500 200ms", List.of(), Map.of());

        var result = orchestrator.orchestrate(input);

        assertThat(result.agentsFound()).isFalse();
        assertThat(result.outputs()).isEmpty();
    }

    @Test
    void orchestrationResult_detectsHumanReviewRequired() {
        var blockOutput = new AgentOutput(CALL_ID, "BlockAgent", AgentDecision.BLOCK,
                0.7, false, "suspicious — low confidence block", Map.of(), null);
        var result = new OrchestrationResult(CALL_ID, AgentCapability.GOVERNANCE,
                List.of(blockOutput), true);

        assertThat(result.requiresHumanReview()).isTrue();
        assertThat(result.hasAutoBlock()).isFalse();
    }

    @Test
    void orchestrationResult_detectsAutoBlock_whenConfidenceHigh() {
        var blockOutput = new AgentOutput(CALL_ID, "BlockAgent", AgentDecision.BLOCK,
                0.95, true, "clear policy violation", Map.of(), null);
        var result = new OrchestrationResult(CALL_ID, AgentCapability.GOVERNANCE,
                List.of(blockOutput), true);

        assertThat(result.hasAutoBlock()).isTrue();
        assertThat(result.highestSeverityDecision()).isEqualTo(AgentDecision.BLOCK);
    }

    private record StubAgent(
            String agentType,
            AgentCapability capability,
            AgentDecision decision,
            double confidence,
            String rationale
    ) implements Agent {

        @Override
        public Set<AgentCapability> capabilities() {
            return Set.of(capability);
        }

        @Override
        public AgentOutput execute(AgentInput input) {
            return new AgentOutput(input.callId(), agentType, decision, confidence,
                    decision == AgentDecision.BLOCK && confidence >= 0.8,
                    rationale, Map.of(), null);
        }
    }
}
