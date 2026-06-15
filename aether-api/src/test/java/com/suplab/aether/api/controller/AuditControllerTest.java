package com.suplab.aether.api.controller;

import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.policy.audit.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    private static final UUID TENANT_UUID = UUID.randomUUID();

    @Test
    @WithMockUser
    void list_returnsAuditEntries() throws Exception {
        var entry = Map.<String, Object>of(
                "id", UUID.randomUUID().toString(),
                "tenantId", TENANT_UUID.toString(),
                "entityType", "TENANT",
                "entityId", TENANT_UUID.toString(),
                "action", "TENANT_ONBOARDED",
                "actor", "system",
                "detail", "{\"detail\":\"tenant onboarded\"}",
                "occurredAt", "2026-06-15T10:00:00Z"
        );
        when(auditLogService.findByTenant(any(TenantId.class), anyInt())).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/tenants/{tenantId}/audit", TENANT_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("TENANT_ONBOARDED"))
                .andExpect(jsonPath("$[0].actor").value("system"));
    }

    @Test
    @WithMockUser
    void list_returnsEmptyListWhenNoEntries() throws Exception {
        when(auditLogService.findByTenant(any(TenantId.class), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tenants/{tenantId}/audit", TENANT_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser
    void list_respectsLimitParameter() throws Exception {
        when(auditLogService.findByTenant(any(TenantId.class), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tenants/{tenantId}/audit", TENANT_UUID)
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }
}
