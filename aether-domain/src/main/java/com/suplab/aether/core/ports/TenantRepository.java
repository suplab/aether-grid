package com.suplab.aether.core.ports;

import com.suplab.aether.core.domain.Tenant;
import com.suplab.aether.core.domain.TenantId;

import java.util.Optional;

public interface TenantRepository {

    void save(Tenant tenant);

    Optional<Tenant> findById(TenantId id);

    Optional<Tenant> findByApiKeyHash(String apiKeyHash);
}
