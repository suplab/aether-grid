package com.suplab.aether.api.controller;

import com.suplab.aether.api.dto.TenantRequest;
import com.suplab.aether.api.dto.TenantResponse;
import com.suplab.aether.core.domain.Tenant;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.exception.TenantNotFoundException;
import com.suplab.aether.core.ports.TenantRepository;
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

    public TenantController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> onboard(@RequestBody @Valid TenantRequest request) {
        var apiKeyHash = sha256Hex(request.apiKey());
        var tenant = Tenant.onboard(request.name(), apiKeyHash);
        tenantRepository.save(tenant);
        log.info("Onboarded tenant id={} name={}", tenant.id(), tenant.name());
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
        var tenant = tenantRepository.findById(TenantId.of(tenantId.toString()))
                .orElseThrow(() -> new TenantNotFoundException(TenantId.of(tenantId.toString())));
        tenant.suspend();
        tenantRepository.save(tenant);
        return ResponseEntity.ok(TenantResponse.from(tenant));
    }

    @PutMapping("/{tenantId}/reactivate")
    public ResponseEntity<TenantResponse> reactivate(@PathVariable UUID tenantId) {
        var tenant = tenantRepository.findById(TenantId.of(tenantId.toString()))
                .orElseThrow(() -> new TenantNotFoundException(TenantId.of(tenantId.toString())));
        tenant.reactivate();
        tenantRepository.save(tenant);
        return ResponseEntity.ok(TenantResponse.from(tenant));
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
