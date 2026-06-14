package com.suplab.aether.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suplab.aether.api.dto.MemorySearchRequest;
import com.suplab.aether.core.domain.MemoryRecord;
import com.suplab.aether.core.domain.MemoryType;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.EmbeddingPort;
import com.suplab.aether.core.ports.MemoryStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemoryController.class)
class MemoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemoryStore memoryStore;

    @MockBean
    private EmbeddingPort embeddingPort;

    private static final UUID TENANT_UUID = UUID.randomUUID();
    private static final float[] FAKE_EMBEDDING = new float[384];

    @Test
    @WithMockUser
    void search_returnsMatchingRecords() throws Exception {
        var memoryId = UUID.randomUUID();
        var record = MemoryRecord.create(
                TenantId.of(TENANT_UUID.toString()),
                MemoryType.SEMANTIC,
                "API call succeeded",
                FAKE_EMBEDDING,
                null
        );

        when(embeddingPort.embed(anyString())).thenReturn(FAKE_EMBEDDING);
        when(memoryStore.findSimilar(any(), any(), anyInt())).thenReturn(List.of(record));

        var request = new MemorySearchRequest("API call", 5);

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/memory/search", TENANT_UUID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("API call succeeded"))
                .andExpect(jsonPath("$[0].memoryType").value("SEMANTIC"));
    }

    @Test
    @WithMockUser
    void search_rejectsBlankQuery() throws Exception {
        var request = new MemorySearchRequest("", 5);

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/memory/search", TENANT_UUID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.query").exists());
    }

    @Test
    @WithMockUser
    void search_rejectsTopKAboveMax() throws Exception {
        var request = new MemorySearchRequest("query", 100);

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/memory/search", TENANT_UUID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void delete_returnsNoContent() throws Exception {
        var memoryId = UUID.randomUUID();
        doNothing().when(memoryStore).delete(any(), any());

        mockMvc.perform(delete("/api/v1/tenants/{tenantId}/memory/{memoryId}", TENANT_UUID, memoryId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
