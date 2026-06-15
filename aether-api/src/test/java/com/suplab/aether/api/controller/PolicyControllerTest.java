package com.suplab.aether.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suplab.aether.api.dto.PolicyRequest;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.PolicyRepository;
import com.suplab.aether.policy.audit.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PolicyRepository policyRepository;

    @MockBean
    private AuditLogService auditLogService;

    private static final UUID TENANT_UUID = UUID.randomUUID();
    private static final UUID POLICY_UUID = UUID.randomUUID();

    @Test
    @WithMockUser
    void create_returnsCreatedPolicy() throws Exception {
        doNothing().when(policyRepository).savePolicy(any(), any(), anyString(), anyString());

        var request = new PolicyRequest("rules:\n  - deny: all", "admin@suplab.io");

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/policies", TENANT_UUID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(TENANT_UUID.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.policyId").isNotEmpty());
    }

    @Test
    @WithMockUser
    void create_rejectsBlankYaml() throws Exception {
        var request = new PolicyRequest("", "admin@suplab.io");

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/policies", TENANT_UUID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.yamlContent").exists());
    }

    @Test
    @WithMockUser
    void activate_returnsActiveStatus() throws Exception {
        doNothing().when(policyRepository).activatePolicy(any(), any());

        mockMvc.perform(put("/api/v1/tenants/{tenantId}/policies/{policyId}/activate", TENANT_UUID, POLICY_UUID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.policyId").value(POLICY_UUID.toString()));
    }

    @Test
    @WithMockUser
    void archive_returnsArchivedStatus() throws Exception {
        doNothing().when(policyRepository).archivePolicy(any(), any());

        mockMvc.perform(put("/api/v1/tenants/{tenantId}/policies/{policyId}/archive", TENANT_UUID, POLICY_UUID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    @WithMockUser
    void getActive_returnsYaml() throws Exception {
        when(policyRepository.findActiveYamlByTenant(TenantId.of(TENANT_UUID.toString())))
                .thenReturn(Optional.of("rules:\n  - deny: all"));

        mockMvc.perform(get("/api/v1/tenants/{tenantId}/policies/active", TENANT_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yaml").value("rules:\n  - deny: all"))
                .andExpect(jsonPath("$.tenantId").value(TENANT_UUID.toString()));
    }

    @Test
    @WithMockUser
    void getActive_returns404WhenNoPolicyActive() throws Exception {
        when(policyRepository.findActiveYamlByTenant(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/tenants/{tenantId}/policies/active", TENANT_UUID))
                .andExpect(status().isNotFound());
    }
}
