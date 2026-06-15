package com.suplab.aether.core.ports;

import com.suplab.aether.core.domain.TenantId;

import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository {

    Optional<String> findActiveYamlByTenant(TenantId tenantId);

    void savePolicy(TenantId tenantId, UUID policyId, String yamlContent, String changedBy);

    void activatePolicy(TenantId tenantId, UUID policyId);

    void archivePolicy(TenantId tenantId, UUID policyId);
}
