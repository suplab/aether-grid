package com.suplab.aether.policy.config;

import com.suplab.aether.core.ports.PolicyRepository;
import com.suplab.aether.policy.audit.AuditLogService;
import com.suplab.aether.policy.engine.SpelPolicyEngine;
import com.suplab.aether.policy.gdpr.GdprRedactionService;
import com.suplab.aether.policy.store.JdbcPolicyRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class PolicyConfig {

    @Bean
    public PolicyRepository policyRepository(NamedParameterJdbcTemplate jdbc) {
        return new JdbcPolicyRepository(jdbc);
    }

    @Bean
    public SpelPolicyEngine spelPolicyEngine(PolicyRepository policyRepository) {
        return new SpelPolicyEngine(policyRepository);
    }

    @Bean
    public GdprRedactionService gdprRedactionService() {
        return new GdprRedactionService();
    }

    @Bean
    public AuditLogService auditLogService(NamedParameterJdbcTemplate jdbc) {
        return new AuditLogService(jdbc);
    }
}
