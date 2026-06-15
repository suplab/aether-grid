package com.suplab.aether.api.controller;

import com.suplab.aether.api.dto.TenantRequest;
import com.suplab.aether.api.dto.TenantResponse;
import com.suplab.aether.core.domain.Tenant;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.exception.TenantNotFoundException;
import com.suplab.aether.core.ports.MemoryStore;
import com.suplab.aether.core.ports.TenantRepository;
import com.suplab.aether.policy.audit.AuditLogService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private static final Logger log = LoggerFactory.getLogger(TenantController.class);

    private final TenantRepository tenantRepository;
    private final AuditLogService auditLogService;
    private final MemoryStore memoryStore;

    public TenantController(TenantRepository tenantRepository,
                            AuditLogService auditLogService,
                            MemoryStore memoryStore) {
        this.tenantRepository = tenantRepository;
        this.auditLogService = auditLogService;
        this.memoryStore = memoryStore;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> onboard(@RequestBody @Valid TenantRequest request) {
        var apiKeyHash = sha256Hex(request.apiKey());
        var tenant = Tenant.onboard(request.name(), apiKeyHash);
        tenantRepository.save(tenant);
        log.info("Onboarded tenant id={} name={}", tenant.id(), tenant.name());
        auditLogService.log(tenant.id(), "TENANT_ONBOARDED", "tenant onboarded with name=" + tenant.name(), "system");
        return ResponseEntity.status(HttpStatus.CREATED).body(TenantResponse.from(tenant));
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> get(@PathVariable UUID tenantId) {
        var tenant = tenantRepository.findById(TenantId.of(tenantId.toString()))
                .orElseThrow(() -> new TenantNotFoundException(TenantId.of(tenantId.toString())));
        return ResponseEntity.ok(TenantResponse.from(tenant));
    }

    @PutMapping("/{tenantId}/suspend")
    public ResponseEntity<TenantResponse> suspend(@PathVariable UUID tenantId) {
        var tid = TenantId.of(tenantId.toString());
        var tenant = tenantRepository.findById(tid)
                .orElseThrow(() -> new TenantNotFoundException(tid));
        tenant.suspend();
        tenantRepository.save(tenant);
        auditLogService.log(tid, "TENANT_SUSPENDED", "tenant suspended", "system");
        return ResponseEntity.ok(TenantResponse.from(tenant));
    }

    @PutMapping("/{tenantId}/reactivate")
    public ResponseEntity<TenantResponse> reactivate(@PathVariable UUID tenantId) {
        var tid = TenantId.of(tenantId.toString());
        var tenant = tenantRepository.findById(tid)
                .orElseThrow(() -> new TenantNotFoundException(tid));
        tenant.reactivate();
        tenantRepository.save(tenant);
        auditLogService.log(tid, "TENANT_REACTIVATED", "tenant reactivated", "system");
        return ResponseEntity.ok(TenantResponse.from(tenant));
    }

    @PutMapping("/{tenantId}/gdpr/memory-opt-out")
    public ResponseEntity<TenantResponse> optOut(@PathVariable UUID tenantId) {
        var tid = TenantId.of(tenantId.toString());
        var tenant = tenantRepository.findById(tid)
                .orElseThrow(() -> new TenantNotFoundException(tid));
        tenant.optOutOfMemory();
        tenantRepository.save(tenant);
        log.info("GDPR memory opt-out set for tenant id={}", tenantId);
        auditLogService.log(tid, "GDPR_MEMORY_OPT_OUT", "memory storage disabled", "system");
        return ResponseEntity.ok(TenantResponse.from(tenant));
    }

    @DeleteMapping("/{tenantId}/gdpr/memory-opt-out")
    public ResponseEntity<TenantResponse> optIn(@PathVariable UUID tenantId) {
        var tid = TenantId.of(tenantId.toString());
        var tenant = tenantRepository.findById(tid)
                .orElseThrow(() -> new TenantNotFoundException(tid));
        tenant.optIntoMemory();
        tenantRepository.save(tenant);
        log.info("GDPR memory opt-in restored for tenant id={}", tenantId);
        auditLogService.log(tid, "GDPR_MEMORY_OPT_IN", "memory storage re-enabled", "system");
        return ResponseEntity.ok(TenantResponse.from(tenant));
    }

    @DeleteMapping("/{tenantId}/memories")
    public ResponseEntity<Void> eraseMemories(@PathVariable UUID tenantId) {
        var tid = TenantId.of(tenantId.toString());
        tenantRepository.findById(tid)
                .orElseThrow(() -> new TenantNotFoundException(tid));
        memoryStore.deleteAll(tid);
        log.info("GDPR right-to-erasure: all memories deleted for tenant id={}", tenantId);
        auditLogService.log(tid, "GDPR_RIGHT_TO_ERASURE", "all memories deleted for tenant", "system");
        return ResponseEntity.noContent().build();
    }

    private static String sha256Hex(String input) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
