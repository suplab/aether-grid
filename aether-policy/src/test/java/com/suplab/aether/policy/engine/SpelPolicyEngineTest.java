package com.suplab.aether.policy.engine;

import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpelPolicyEngineTest {

    @Mock
    private PolicyRepository policyRepository;

    private SpelPolicyEngine engine;
    private TenantId tenantId;

    @BeforeEach
    void setUp() {
        engine = new SpelPolicyEngine(policyRepository);
        tenantId = TenantId.generate();
    }

    @Test
    void allows_when_no_active_policy() {
        when(policyRepository.findActiveYamlByTenant(any())).thenReturn(Optional.empty());

        var result = engine.evaluate(tenantId, buildCtx("GET", "/v1/data", 200, 50L, "SUCCESS"));

        assertThat(result.overallAction()).isEqualTo(PolicyAction.ALLOW);
        assertThat(result.matches()).isEmpty();
    }

    @Test
    void blocks_when_rule_matches_high_latency() {
        when(policyRepository.findActiveYamlByTenant(any())).thenReturn(Optional.of("""
                rules:
                  - name: high-latency-block
                    description: Block requests with latency > 10000ms
                    condition: latencyMs > 10000
                    action: BLOCK
                    priority: 10
                """));

        var result = engine.evaluate(tenantId, buildCtx("GET", "/v1/data", 200, 15000L, "SUCCESS"));

        assertThat(result.overallAction()).isEqualTo(PolicyAction.BLOCK);
        assertThat(result.isBlocked()).isTrue();
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().getFirst().ruleName()).isEqualTo("high-latency-block");
    }

    @Test
    void alerts_when_rule_matches_error_path() {
        when(policyRepository.findActiveYamlByTenant(any())).thenReturn(Optional.of("""
                rules:
                  - name: server-error-alert
                    description: Alert on 5xx responses
                    condition: responseCode >= 500
                    action: ALERT
                    priority: 5
                """));

        var result = engine.evaluate(tenantId, buildCtx("POST", "/v1/users", 503, 200L, "FAILURE"));

        assertThat(result.overallAction()).isEqualTo(PolicyAction.ALERT);
        assertThat(result.hasAlerts()).isTrue();
    }

    @Test
    void allows_when_no_rules_match() {
        when(policyRepository.findActiveYamlByTenant(any())).thenReturn(Optional.of("""
                rules:
                  - name: only-post
                    description: Only matches POST
                    condition: method == 'POST'
                    action: BLOCK
                    priority: 1
                """));

        var result = engine.evaluate(tenantId, buildCtx("GET", "/v1/data", 200, 50L, "SUCCESS"));

        assertThat(result.overallAction()).isEqualTo(PolicyAction.ALLOW);
    }

    @Test
    void block_takes_priority_over_alert() {
        when(policyRepository.findActiveYamlByTenant(any())).thenReturn(Optional.of("""
                rules:
                  - name: always-alert
                    condition: "true"
                    action: ALERT
                    priority: 1
                  - name: always-block
                    condition: responseCode >= 500
                    action: BLOCK
                    priority: 10
                """));

        var result = engine.evaluate(tenantId, buildCtx("POST", "/v1/data", 500, 100L, "FAILURE"));

        assertThat(result.overallAction()).isEqualTo(PolicyAction.BLOCK);
    }

    @Test
    void handles_malformed_policy_yaml_gracefully() {
        when(policyRepository.findActiveYamlByTenant(any())).thenReturn(Optional.of("not: valid: yaml: [[["));

        var result = engine.evaluate(tenantId, buildCtx("GET", "/v1/data", 200, 50L, "SUCCESS"));

        assertThat(result.overallAction()).isEqualTo(PolicyAction.ALLOW);
    }

    private PolicyEvaluationContext buildCtx(String method, String path, int code, long latencyMs, String outcome) {
        return new PolicyEvaluationContext(method, path, code, latencyMs, outcome,
                tenantId.toString(), Map.of(), Map.of());
    }
}
