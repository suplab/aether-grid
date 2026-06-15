package com.suplab.aether.policy.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SpelPolicyEngine {

    private static final Logger log = LoggerFactory.getLogger(SpelPolicyEngine.class);

    private final PolicyRepository policyRepository;
    private final SpelExpressionParser parser;
    private final ObjectMapper yamlMapper;

    public SpelPolicyEngine(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
        this.parser = new SpelExpressionParser();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    public PolicyEvaluationResult evaluate(TenantId tenantId, PolicyEvaluationContext ctx) {
        var yamlOpt = policyRepository.findActiveYamlByTenant(tenantId);
        if (yamlOpt.isEmpty()) {
            log.debug("No active policy for tenant={} — defaulting to ALLOW", tenantId);
            return PolicyEvaluationResult.allow();
        }

        var rules = parseRules(yamlOpt.get());
        if (rules.isEmpty()) return PolicyEvaluationResult.allow();

        var matches = new ArrayList<PolicyEvaluationResult.RuleMatch>();
        var evalCtx = buildEvaluationContext(ctx);

        rules.stream()
                .sorted(Comparator.comparingInt(PolicyRule::priority).reversed())
                .forEach(rule -> {
                    try {
                        var expr = parser.parseExpression(rule.condition());
                        var result = Boolean.TRUE.equals(expr.getValue(evalCtx, Boolean.class));
                        if (result) {
                            matches.add(new PolicyEvaluationResult.RuleMatch(
                                    rule.name(), rule.action(), rule.description()));
                            log.debug("Policy rule '{}' matched → {}", rule.name(), rule.action());
                        }
                    } catch (Exception e) {
                        log.warn("Failed to evaluate policy rule '{}': {}", rule.name(), e.getMessage());
                    }
                });

        var overallAction = determineOverallAction(matches);
        return new PolicyEvaluationResult(overallAction, List.copyOf(matches), null);
    }

    private List<PolicyRule> parseRules(String yaml) {
        try {
            var parsed = yamlMapper.readValue(yaml, new TypeReference<Map<String, Object>>() {});
            @SuppressWarnings("unchecked")
            var rulesRaw = (List<Map<String, Object>>) parsed.getOrDefault("rules", List.of());
            return rulesRaw.stream()
                    .map(r -> new PolicyRule(
                            (String) r.get("name"),
                            (String) r.getOrDefault("description", ""),
                            (String) r.get("condition"),
                            PolicyAction.valueOf(((String) r.getOrDefault("action", "ALLOW")).toUpperCase()),
                            (int) r.getOrDefault("priority", 0)
                    ))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to parse policy YAML: {}", e.getMessage());
            return List.of();
        }
    }

    private EvaluationContext buildEvaluationContext(PolicyEvaluationContext ctx) {
        return SimpleEvaluationContext.forReadOnlyDataBinding()
                .withRootObject(ctx)
                .build();
    }

    private PolicyAction determineOverallAction(List<PolicyEvaluationResult.RuleMatch> matches) {
        if (matches.stream().anyMatch(m -> m.action() == PolicyAction.BLOCK)) return PolicyAction.BLOCK;
        if (matches.stream().anyMatch(m -> m.action() == PolicyAction.RATE_LIMIT)) return PolicyAction.RATE_LIMIT;
        if (matches.stream().anyMatch(m -> m.action() == PolicyAction.ALERT)) return PolicyAction.ALERT;
        if (matches.stream().anyMatch(m -> m.action() == PolicyAction.AUDIT)) return PolicyAction.AUDIT;
        return PolicyAction.ALLOW;
    }
}
