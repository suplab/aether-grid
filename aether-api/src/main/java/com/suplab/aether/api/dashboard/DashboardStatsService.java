package com.suplab.aether.api.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class DashboardStatsService {

    private static final Logger log = LoggerFactory.getLogger(DashboardStatsService.class);

    private final NamedParameterJdbcTemplate jdbc;

    public DashboardStatsService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> getSystemStats() {
        log.debug("Fetching dashboard system stats snapshot");

        var tenantCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tenants WHERE status = 'ACTIVE'",
                Map.of(), Long.class);
        var memoryCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM memory_embeddings",
                Map.of(), Long.class);
        var policyCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM policies WHERE status = 'ACTIVE'",
                Map.of(), Long.class);
        var decisionsToday = jdbc.queryForObject(
                "SELECT COUNT(*) FROM agent_decisions WHERE created_at >= NOW() - INTERVAL '24 hours'",
                Map.of(), Long.class);
        var auditEventsToday = jdbc.queryForObject(
                "SELECT COUNT(*) FROM audit_log WHERE occurred_at >= NOW() - INTERVAL '24 hours'",
                Map.of(), Long.class);

        return Map.of(
                "activeTenants", tenantCount != null ? tenantCount : 0L,
                "totalMemories", memoryCount != null ? memoryCount : 0L,
                "activePolicies", policyCount != null ? policyCount : 0L,
                "decisionsLast24h", decisionsToday != null ? decisionsToday : 0L,
                "auditEventsLast24h", auditEventsToday != null ? auditEventsToday : 0L,
                "timestamp", Instant.now().toString()
        );
    }

    public List<Map<String, Object>> getRecentDecisions(int limit) {
        log.debug("Fetching recent agent decisions limit={}", limit);
        var sql = """
                SELECT id, tenant_id, agent_type, decision, confidence, rationale, auto_enforced, created_at
                FROM agent_decisions
                ORDER BY created_at DESC
                LIMIT :limit
                """;
        return jdbc.queryForList(sql, Map.of("limit", limit));
    }

    public List<Map<String, Object>> getMemoryTypeBreakdown() {
        log.debug("Fetching memory type breakdown");
        var sql = """
                SELECT memory_type, COUNT(*) AS count, ROUND(AVG(strength)::numeric, 3) AS avg_strength
                FROM memory_embeddings
                GROUP BY memory_type
                ORDER BY count DESC
                """;
        return jdbc.queryForList(sql, Map.of());
    }

    public List<Map<String, Object>> getAgentDecisionBreakdown() {
        log.debug("Fetching agent decision breakdown for last 7 days");
        var sql = """
                SELECT agent_type, decision, COUNT(*) AS count,
                       ROUND(AVG(confidence)::numeric, 3) AS avg_confidence
                FROM agent_decisions
                WHERE created_at >= NOW() - INTERVAL '7 days'
                GROUP BY agent_type, decision
                ORDER BY agent_type, count DESC
                """;
        return jdbc.queryForList(sql, Map.of());
    }
}
