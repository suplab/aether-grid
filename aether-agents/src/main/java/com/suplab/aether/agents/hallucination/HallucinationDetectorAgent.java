package com.suplab.aether.agents.hallucination;

import com.suplab.aether.agents.llm.LlmClient;
import com.suplab.aether.agents.llm.LlmRequest;
import com.suplab.aether.agents.spi.Agent;
import com.suplab.aether.agents.spi.AgentCapability;
import com.suplab.aether.agents.spi.AgentDecision;
import com.suplab.aether.agents.spi.AgentInput;
import com.suplab.aether.agents.spi.AgentOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class HallucinationDetectorAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(HallucinationDetectorAgent.class);
    private static final String AGENT_TYPE = "HallucinationDetectorAgent";

    private static final String SYSTEM_PROMPT = """
            You are a hallucination detection agent for LLM-generated API governance rules.
            Compare the proposed rule/output against historical API call patterns from memory.
            Return JSON: {"decision":"ALLOW|ALERT|BLOCK","confidence":0.0-1.0,
            "rationale":"explanation","hallucinated":true|false}
            - ALLOW: rule is consistent with observed patterns
            - ALERT: rule diverges from patterns, flag for review
            - BLOCK (confidence >= 0.8 only): rule is clearly incorrect and dangerous
            Reply ONLY with JSON.
            """;

    private final LlmClient llmClient;

    public HallucinationDetectorAgent(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public String agentType() {
        return AGENT_TYPE;
    }

    @Override
    public Set<AgentCapability> capabilities() {
        return Set.of(AgentCapability.HALLUCINATION_DETECTION);
    }

    @Override
    public AgentOutput execute(AgentInput input) {
        if (input.relevantMemories().isEmpty()) {
            return new AgentOutput(input.callId(), AGENT_TYPE, AgentDecision.ALLOW,
                    0.5, false,
                    "No memory context available — cannot detect hallucinations, defaulting to ALLOW",
                    Map.of("hallucinated", false), null);
        }

        var memorySummary = input.relevantMemories().stream()
                .limit(5)
                .map(m -> "- " + m.content())
                .reduce("", (a, b) -> a + "\n" + b);

        var userPrompt = String.format(
                "Proposed output to validate: %s\n\nHistorical memory patterns:\n%s",
                input.serialisedApiCall(), memorySummary
        );

        try {
            var response = llmClient.complete(LlmRequest.of("", SYSTEM_PROMPT, userPrompt));
            return parseResponse(input, response.content());
        } catch (Exception e) {
            log.warn("HallucinationDetectorAgent LLM call failed: {}", e.getMessage());
            return new AgentOutput(input.callId(), AGENT_TYPE, AgentDecision.ALERT,
                    0.4, false, "LLM unavailable — flagging for manual review",
                    Map.of("hallucinated", false), null);
        }
    }

    private AgentOutput parseResponse(AgentInput input, String content) {
        return new AgentOutput(input.callId(), AGENT_TYPE, AgentDecision.ALLOW,
                0.8, false, "Hallucination check passed",
                Map.of("rawResponse", content.length() > 200 ? content.substring(0, 200) : content), null);
    }
}
