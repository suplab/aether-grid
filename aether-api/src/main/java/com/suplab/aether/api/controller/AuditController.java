package com.suplab.aether.api.controller;

import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.policy.audit.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/audit")
public class AuditController {

    private static final Logger log = LoggerFactory.getLogger(AuditController.class);

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "50") int limit) {
        var tid = TenantId.of(tenantId.toString());
        log.debug("Fetching audit log for tenant={} limit={}", tenantId, limit);
        var entries = auditLogService.findByTenant(tid, limit);
        return ResponseEntity.ok(entries);
    }
}
