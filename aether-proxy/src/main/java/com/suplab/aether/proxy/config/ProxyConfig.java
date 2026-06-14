package com.suplab.aether.proxy.config;

import com.suplab.aether.proxy.filter.ApiCallCaptureFilter;
import com.suplab.aether.proxy.filter.RedactionFilter;
import com.suplab.aether.proxy.filter.TenantAuthFilter;
import com.suplab.aether.proxy.outbox.OutboxRelayScheduler;
import com.suplab.aether.proxy.ratelimit.TenantKeyResolver;
import com.suplab.aether.proxy.repository.JdbcOutboxRepository;
import com.suplab.aether.proxy.repository.JdbcTenantRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class ProxyConfig {

    @Bean
    public JdbcTenantRepository tenantRepository(NamedParameterJdbcTemplate jdbc) {
        return new JdbcTenantRepository(jdbc);
    }

    @Bean
    public JdbcOutboxRepository outboxRepository(NamedParameterJdbcTemplate jdbc) {
        return new JdbcOutboxRepository(jdbc);
    }

    @Bean
    public TenantAuthFilter tenantAuthFilter(JdbcTenantRepository tenantRepository) {
        return new TenantAuthFilter(tenantRepository);
    }

    @Bean
    public RedactionFilter redactionFilter() {
        return new RedactionFilter();
    }

    @Bean
    public ApiCallCaptureFilter apiCallCaptureFilter(JdbcOutboxRepository outboxRepository) {
        return new ApiCallCaptureFilter(outboxRepository);
    }

    @Bean
    public TenantKeyResolver tenantKeyResolver() {
        return new TenantKeyResolver();
    }

    @Bean
    public OutboxRelayScheduler outboxRelayScheduler(JdbcOutboxRepository outboxRepository,
                                                      KafkaTemplate<String, String> kafkaTemplate) {
        return new OutboxRelayScheduler(outboxRepository, kafkaTemplate);
    }
}
