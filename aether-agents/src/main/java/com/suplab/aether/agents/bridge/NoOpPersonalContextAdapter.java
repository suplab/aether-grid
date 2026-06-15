package com.suplab.aether.agents.bridge;

import com.suplab.aether.core.domain.PersonalContext;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.PersonalContextPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnMissingBean(PersonalContextPort.class)
public class NoOpPersonalContextAdapter implements PersonalContextPort {

    @Override
    public Optional<PersonalContext> fetchFor(TenantId tenantId, String userId) {
        return Optional.empty();
    }
}
