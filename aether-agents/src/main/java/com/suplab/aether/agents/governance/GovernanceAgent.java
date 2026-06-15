package com.suplab.aether.agents.governance;

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

public class GovernanceAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(GovernanceAgent.class);
    private static final String AGENT_TYPE = "GovernanceAgent";

    private static final String SYSTEM_PROMPT = """
            You are an API governance agent. Analyse the API call and relevant memory context.
            Return JSON with exactly two fields:
            {"decision":"ALLOW|BLOCK|ALERT|SUGGEST","confidence":0.0-1.0,"rationale":"brief reason"}
            Decision rules:
            - ALLOW: call looks normal based on historical patterns
            - ALERT: suspicious parameters or unusual patterns detected
            - SUGGEST: sub-optimal usage pattern, suggest improvement
            - BLOCK: only if clear policy violation with confidence >= 0.8
            Keep rationale under 200 chars. Reply ONLY with JSON, no markdown.
            """;

    private final LlmClient llmClient;

    public GovernanceAgent(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public String agentType() {
        return AGENT_TYPE;
    }

    @Override
    public Set<AgentCapability> capabilities() {
        return Set.of(AgentCapability.GOVERNANCE);
    }

    @Override
    public AgentOutput execute(AgentInput input) {
        var userPrompt = buildPrompt(input);
        var request = LlmRequest.of(
                llmClient.provider().name().toLowerCase() + ":governance",
                SYSTEM_PROMPT,
                userPrompt
        );

        try {
            var response = llmClient.complete(request);
            return parseResponse(input, response.content());
        } catch (Exception e) {
            log.warn("GovernanceAgent LLM call failed for callId={}: {}", input.callId(), e.getMessage());
            return allowWithLowConfidence(input, "LLM unavailable — defaulting to ALLOW");
        }
    }

    private String buildPrompt(AgentInput input) {
        var memoryCount = input.relevantMemories().size();
        return String.format(
                "API call: %s\nRelevant memory records: %d\nCall context: %s",
                input.serialisedApiCall(),
                memoryCount,
                input.context().isEmpty() ? "none" : input.context().toString()
        );
    }

    private AgentOutput parseResponse(AgentInput input, String llmContent) {
        try {
            var json = extractJson(llmContent);
            var decision = parseDecision(json);
            var confidence = parseConfidence(json);
            var rationale = parseRationale(json);
            return new AgentOutput(
                    input.callId(), AGENT_TYPE, decision, confidence,
                    decision == AgentDecision.BLOCK && confidence >= 0.8,
                    rationale, Map.of("provider", llmClient.provider().name()), null
            );
        } catch (Exception e) {
            log.warn("Failed to parse GovernanceAgent LLM response: {}", e.getMessage());
            return allowWithLowConfidence(input, "Parse error — defaulting to ALLOW");
        }
    }

    private AgentOutput allowWithLowConfidence(AgentInput input, String rationale) {
        return new AgentOutput(input.callId(), AGENT_TYPE, AgentDecision.ALLOW,
                0.5, false, rationale, Map.of(), null);
    }

    private String extractJson(String content) {
        var start = content.indexOf('{');
        var end = content.lastIndexOf('}');
        if (start < 0 || end < 0 || end < start) throw new IllegalArgumentException("No JSON object found in LLM response");
        return content.substring(start, end + 1);
    }

    private AgentDecision parseDecision(String json) {
        var match = extractStringValue(json, "decision");
        return AgentDecision.valueOf(match.toUpperCase());
    }

    private double parseConfidence(String json) {
        var match = extractNumberValue(json, "confidence");
        return Double.parseDouble(match);
    }

    private String parseRationale(String json) {
        return extractStringValue(json, "rationale");
    }

    private String extractStringValue(String json, String key) {
        var pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        var matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("Key not found: " + key);
        return matcher.group(1);
    }

    private String extractNumberValue(String json, String key) {
        var pattern = "\"" + key + "\"\\s*:\\s*([0-9.]+)";
        var matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("Key not found: " + key);
        return matcher.group(1);
    }
}
