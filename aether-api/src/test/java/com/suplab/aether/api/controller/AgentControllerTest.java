package com.suplab.aether.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suplab.aether.api.dto.FeedbackRequest;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.AgentFeedbackPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgentFeedbackPort feedbackPort;

    private static final UUID TENANT_UUID = UUID.randomUUID();
    private static final UUID DECISION_UUID = UUID.randomUUID();

    @Test
    @WithMockUser
    void recordFeedback_returns201() throws Exception {
        doNothing().when(feedbackPort).record(any());

        var request = new FeedbackRequest(
                DECISION_UUID,
                "GovernanceAgent",
                "BLOCK",
                0.85,
                "INCORRECT",
                "False positive — legitimate API call was blocked"
        );

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/agents/feedback", TENANT_UUID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.feedbackId").isNotEmpty());
    }

    @Test
    @WithMockUser
    void recordFeedback_rejectsInvalidOutcome() throws Exception {
        var request = new FeedbackRequest(
                DECISION_UUID,
                "GovernanceAgent",
                "BLOCK",
                0.85,
                "NOT_A_REAL_OUTCOME",
                null
        );

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/agents/feedback", TENANT_UUID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getPerformance_returnsStats() throws Exception {
        var agentStats = Map.<String, Object>of(
                "total", 100L,
                "correct", 87L,
                "accuracy", 87.0
        );
        when(feedbackPort.getPerformanceStats(any(TenantId.class)))
                .thenReturn(Map.of("GovernanceAgent", agentStats));

        mockMvc.perform(get("/api/v1/tenants/{tenantId}/agents/performance", TENANT_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(TENANT_UUID.toString()))
                .andExpect(jsonPath("$.agents.GovernanceAgent.total").value(100))
                .andExpect(jsonPath("$.agents.GovernanceAgent.correct").value(87));
    }
}
