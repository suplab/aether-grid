package com.suplab.aether.api.service;

import com.suplab.aether.agents.registry.AgentRegistry;
import com.suplab.aether.agents.selfimproving.SelfImprovingAgent;
import com.suplab.aether.core.ports.AgentFeedbackPort;
import com.suplab.aether.core.ports.TenantRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LearningConfig {

    @Bean
    public AgentLearningService agentLearningService(AgentRegistry agentRegistry,
                                                     AgentFeedbackPort feedbackPort,
                                                     TenantRepository tenantRepository,
                                                     SelfImprovingAgent selfImprovingAgent) {
        return new AgentLearningService(agentRegistry, feedbackPort, tenantRepository, selfImprovingAgent);
    }
}
