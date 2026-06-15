package com.suplab.aether.agents.orchestrator;

import com.suplab.aether.agents.spi.AgentCapability;
import com.suplab.aether.agents.spi.AgentDecision;
import com.suplab.aether.agents.spi.AgentOutput;
import com.suplab.aether.core.domain.ApiCallId;

import java.util.List;

public record OrchestrationResult(
        ApiCallId callId,
        AgentCapability capability,
        List<AgentOutput> outputs,
        boolean agentsFound
) {

    public static OrchestrationResult of(ApiCallId callId, AgentCapability capability, List<AgentOutput> outputs) {
        return new OrchestrationResult(callId, capability, List.copyOf(outputs), true);
    }

    public static OrchestrationResult noAgents(ApiCallId callId, AgentCapability capability) {
        return new OrchestrationResult(callId, capability, List.of(), false);
    }

    public boolean requiresHumanReview() {
        return outputs.stream().anyMatch(AgentOutput::requiresHumanReview);
    }

    public boolean hasAutoBlock() {
        return outputs.stream().anyMatch(o -> o.decision() == AgentDecision.BLOCK && o.autoEnforced());
    }

    public AgentDecision highestSeverityDecision() {
        if (hasAutoBlock()) return AgentDecision.BLOCK;
        if (outputs.stream().anyMatch(o -> o.decision() == AgentDecision.ALERT)) return AgentDecision.ALERT;
        if (outputs.stream().anyMatch(o -> o.decision() == AgentDecision.SUGGEST)) return AgentDecision.SUGGEST;
        if (outputs.stream().anyMatch(o -> o.decision() == AgentDecision.DEFER)) return AgentDecision.DEFER;
        return AgentDecision.ALLOW;
    }
}
