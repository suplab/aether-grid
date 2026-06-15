package com.suplab.aether.core.ports;

import com.suplab.aether.core.domain.ApiCall;
import com.suplab.aether.core.domain.ApiCallId;
import com.suplab.aether.core.domain.TenantId;

import java.util.List;
import java.util.Optional;

public interface ApiCallRepository {

    void save(ApiCall apiCall);

    Optional<ApiCall> findById(ApiCallId id);

    List<ApiCall> findRecentByTenant(TenantId tenantId, int limit);
}
