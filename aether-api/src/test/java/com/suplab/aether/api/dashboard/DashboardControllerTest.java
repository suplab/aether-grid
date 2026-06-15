package com.suplab.aether.api.dashboard;

import com.suplab.aether.agents.registry.AgentRegistry;
import com.suplab.aether.policy.audit.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardStatsService statsService;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private AgentRegistry agentRegistry;

    @Test
    @WithMockUser
    void stats_returnsSystemStats() throws Exception {
        var statsMap = Map.<String, Object>of(
                "activeTenants", 3L,
                "totalMemories", 142L,
                "activePolicies", 5L,
                "decisionsLast24h", 27L,
                "auditEventsLast24h", 54L,
                "timestamp", Instant.now().toString()
        );
        when(statsService.getSystemStats()).thenReturn(statsMap);

        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.activeTenants").value(3))
                .andExpect(jsonPath("$.totalMemories").value(142))
                .andExpect(jsonPath("$.activePolicies").value(5))
                .andExpect(jsonPath("$.decisionsLast24h").value(27))
                .andExpect(jsonPath("$.auditEventsLast24h").value(54));
    }

    @Test
    @WithMockUser
    void recentDecisions_returnsList() throws Exception {
        var decision = Map.<String, Object>of(
                "id", "00000000-0000-0000-0000-000000000001",
                "tenant_id", "00000000-0000-0000-0000-000000000002",
                "agent_type", "PolicyEnforcementAgent",
                "decision", "ALLOW",
                "confidence", 0.92,
                "rationale", "Policy matched",
                "auto_enforced", true,
                "created_at", Instant.now().toString()
        );
        when(statsService.getRecentDecisions(anyInt())).thenReturn(List.of(decision));

        mockMvc.perform(get("/dashboard/decisions").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].agent_type").value("PolicyEnforcementAgent"))
                .andExpect(jsonPath("$[0].decision").value("ALLOW"));
    }

    @Test
    @WithMockUser
    void agentBreakdown_returnsList() throws Exception {
        var row = Map.<String, Object>of(
                "agent_type", "ThreatAgent",
                "decision", "BLOCK",
                "count", 12L,
                "avg_confidence", 0.876
        );
        when(statsService.getAgentDecisionBreakdown()).thenReturn(List.of(row));

        mockMvc.perform(get("/dashboard/agent-breakdown"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].agent_type").value("ThreatAgent"))
                .andExpect(jsonPath("$[0].decision").value("BLOCK"))
                .andExpect(jsonPath("$[0].count").value(12));
    }

    @Test
    @WithMockUser
    void stream_returnsSseEvent() throws Exception {
        when(statsService.getSystemStats()).thenReturn(Map.of(
                "activeTenants", 1L,
                "totalMemories", 0L,
                "activePolicies", 0L,
                "decisionsLast24h", 0L,
                "auditEventsLast24h", 0L,
                "timestamp", Instant.now().toString()
        ));

        mockMvc.perform(get("/dashboard/stream").accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        containsString(MediaType.TEXT_EVENT_STREAM_VALUE)));
    }
}
