package com.suplab.aether.core.ports;

import com.suplab.aether.core.domain.PersonalContext;
import com.suplab.aether.core.domain.TenantId;

import java.util.Optional;

public interface PersonalContextPort {
    Optional<PersonalContext> fetchFor(TenantId tenantId, String userId);
}
