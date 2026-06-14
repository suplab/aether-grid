package com.suplab.aether.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suplab.aether.api.dto.TenantRequest;
import com.suplab.aether.core.domain.Tenant;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.domain.TenantStatus;
import com.suplab.aether.core.exception.TenantNotFoundException;
import com.suplab.aether.core.ports.TenantRepository;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TenantController.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantRepository tenantRepository;

    private static final UUID TENANT_UUID = UUID.randomUUID();
    private static final String VALID_API_KEY = "a".repeat(32);

    @Test
    @WithMockUser
    void onboard_createsNewTenant() throws Exception {
        doNothing().when(tenantRepository).save(any());

        var request = new TenantRequest("Acme Corp", VALID_API_KEY);

        mockMvc.perform(post("/api/v1/tenants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acme Corp"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @WithMockUser
    void onboard_rejectsBlankName() throws Exception {
        var request = new TenantRequest("", VALID_API_KEY);

        mockMvc.perform(post("/api/v1/tenants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @WithMockUser
    void onboard_rejectsShortApiKey() throws Exception {
        var request = new TenantRequest("Acme Corp", "short");

        mockMvc.perform(post("/api/v1/tenants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void get_returnsTenant() throws Exception {
        var tenant = Tenant.reconstitute(
                TenantId.of(TENANT_UUID.toString()),
                "Acme Corp",
                "hash123",
                TenantStatus.ACTIVE
        );
        when(tenantRepository.findById(TenantId.of(TENANT_UUID.toString()))).thenReturn(Optional.of(tenant));

        mockMvc.perform(get("/api/v1/tenants/{id}", TENANT_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Corp"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    void get_returns404ForMissingTenant() throws Exception {
        when(tenantRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/tenants/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://aether.suplab.io/errors/tenant-not-found"));
    }

    @Test
    @WithMockUser
    void suspend_updatesTenantStatus() throws Exception {
        var tenant = Tenant.reconstitute(
                TenantId.of(TENANT_UUID.toString()),
                "Acme Corp",
                "hash123",
                TenantStatus.ACTIVE
        );
        when(tenantRepository.findById(TenantId.of(TENANT_UUID.toString()))).thenReturn(Optional.of(tenant));
        doNothing().when(tenantRepository).save(any());

        mockMvc.perform(put("/api/v1/tenants/{id}/suspend", TENANT_UUID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    @WithMockUser
    void reactivate_updatesTenantStatus() throws Exception {
        var tenant = Tenant.reconstitute(
                TenantId.of(TENANT_UUID.toString()),
                "Acme Corp",
                "hash123",
                TenantStatus.SUSPENDED
        );
        when(tenantRepository.findById(TenantId.of(TENANT_UUID.toString()))).thenReturn(Optional.of(tenant));
        doNothing().when(tenantRepository).save(any());

        mockMvc.perform(put("/api/v1/tenants/{id}/reactivate", TENANT_UUID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
