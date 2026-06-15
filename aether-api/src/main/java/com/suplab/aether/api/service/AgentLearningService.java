package com.suplab.aether.api.service;

import com.suplab.aether.agents.registry.AgentRegistry;
import com.suplab.aether.agents.selfimproving.SelfImprovingAgent;
import com.suplab.aether.agents.spi.AgentCapability;
import com.suplab.aether.agents.spi.AgentInput;
import com.suplab.aether.core.domain.ApiCallId;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.AgentFeedbackPort;
import com.suplab.aether.core.ports.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;

public class AgentLearningService {

    private static final Logger log = LoggerFactory.getLogger(AgentLearningService.class);

    private final AgentRegistry agentRegistry;
    private final AgentFeedbackPort feedbackPort;
    private final TenantRepository tenantRepository;
    private final SelfImprovingAgent selfImprovingAgent;

    public AgentLearningService(AgentRegistry agentRegistry,
                                AgentFeedbackPort feedbackPort,
                                TenantRepository tenantRepository,
                                SelfImprovingAgent selfImprovingAgent) {
        this.agentRegistry = agentRegistry;
        this.feedbackPort = feedbackPort;
        this.tenantRepository = tenantRepository;
        this.selfImprovingAgent = selfImprovingAgent;
    }

    @Scheduled(cron = "0 0 3 * * SUN")
    public void runWeeklyReview() {
        log.info("AgentLearningService: starting weekly self-improvement review");

        var knownAgentTypes = agentRegistry.agentCapabilityMap().keySet().stream().toList();
        log.info("AgentLearningService: reviewing {} agent type(s): {}", knownAgentTypes.size(), knownAgentTypes);

        var syntheticTenantId = TenantId.generate();
        var input = new AgentInput(
                ApiCallId.generate(),
                syntheticTenantId,
                AgentCapability.SELF_IMPROVEMENT,
                "weekly-review",
                List.of(),
                Map.of("agentTypes", knownAgentTypes)
        );

        var output = selfImprovingAgent.execute(input);
        log.info("AgentLearningService: self-improvement review complete decision={} confidence={} rationale={}",
                output.decision(), output.confidence(), output.rationale());
    }
}
