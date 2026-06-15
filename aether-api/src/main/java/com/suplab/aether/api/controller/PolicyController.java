package com.suplab.aether.api.controller;

import com.suplab.aether.api.dto.PolicyRequest;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.PolicyRepository;
import com.suplab.aether.policy.audit.AuditLogService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/policies")
public class PolicyController {

    private static final Logger log = LoggerFactory.getLogger(PolicyController.class);

    private final PolicyRepository policyRepository;
    private final AuditLogService auditLogService;

    public PolicyController(PolicyRepository policyRepository, AuditLogService auditLogService) {
        this.policyRepository = policyRepository;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable UUID tenantId,
            @RequestBody @Valid PolicyRequest request) {
        var tid = TenantId.of(tenantId.toString());
        var policyId = UUID.randomUUID();
        policyRepository.savePolicy(tid, policyId, request.yamlContent(), request.changedBy());
        log.info("Created policy id={} tenant={} by={}", policyId, tenantId, request.changedBy());
        auditLogService.log(tenantId.toString(), "POLICY", policyId.toString(),
                "POLICY_CREATED", request.changedBy(), Map.of("policyId", policyId.toString(), "status", "DRAFT"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "policyId", policyId.toString(),
                "tenantId", tenantId.toString(),
                "status", "DRAFT"
        ));
    }

    @PutMapping("/{policyId}/activate")
    public ResponseEntity<Map<String, Object>> activate(
            @PathVariable UUID tenantId,
            @PathVariable UUID policyId) {
        var tid = TenantId.of(tenantId.toString());
        policyRepository.activatePolicy(tid, policyId);
        log.info("Activated policy id={} tenant={}", policyId, tenantId);
        auditLogService.log(tenantId.toString(), "POLICY", policyId.toString(),
                "POLICY_ACTIVATED", "system", Map.of("policyId", policyId.toString(), "status", "ACTIVE"));
        return ResponseEntity.ok(Map.of(
                "policyId", policyId.toString(),
                "status", "ACTIVE"
        ));
    }

    @PutMapping("/{policyId}/archive")
    public ResponseEntity<Map<String, Object>> archive(
            @PathVariable UUID tenantId,
            @PathVariable UUID policyId) {
        var tid = TenantId.of(tenantId.toString());
        policyRepository.archivePolicy(tid, policyId);
        log.info("Archived policy id={} tenant={}", policyId, tenantId);
        auditLogService.log(tenantId.toString(), "POLICY", policyId.toString(),
                "POLICY_ARCHIVED", "system", Map.of("policyId", policyId.toString(), "status", "ARCHIVED"));
        return ResponseEntity.ok(Map.of(
                "policyId", policyId.toString(),
                "status", "ARCHIVED"
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActive(@PathVariable UUID tenantId) {
        var tid = TenantId.of(tenantId.toString());
        return policyRepository.findActiveYamlByTenant(tid)
                .map(yaml -> ResponseEntity.ok(Map.<String, Object>of(
                        "tenantId", tenantId.toString(),
                        "yaml", yaml
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
