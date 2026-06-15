package com.suplab.aether.api.config;

import com.suplab.aether.api.dashboard.DashboardStatsService;
import com.suplab.aether.core.domain.AgentFeedback;
import com.suplab.aether.core.domain.DecisionOutcome;
import com.suplab.aether.core.domain.Tenant;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.domain.TenantStatus;
import com.suplab.aether.core.ports.AgentFeedbackPort;
import com.suplab.aether.core.ports.TenantRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Configuration
public class ApiConfig {

    @Bean
    public DashboardStatsService dashboardStatsService(NamedParameterJdbcTemplate jdbc) {
        return new DashboardStatsService(jdbc);
    }

    @Bean
    public TenantRepository tenantRepository(NamedParameterJdbcTemplate jdbc) {
        return new JdbcApiTenantRepository(jdbc);
    }

    @Bean
    public AgentFeedbackPort agentFeedbackPort(NamedParameterJdbcTemplate jdbc) {
        return new JdbcAgentFeedbackRepository(jdbc);
    }

    private static final class JdbcApiTenantRepository implements TenantRepository {

        private final NamedParameterJdbcTemplate jdbc;

        private JdbcApiTenantRepository(NamedParameterJdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        @Override
        public Optional<Tenant> findByApiKeyHash(String apiKeyHash) {
            var sql = """
                    SELECT id, name, api_key_hash, status, memory_opt_out
                    FROM tenants
                    WHERE api_key_hash = :apiKeyHash
                    """;
            var params = new MapSqlParameterSource("apiKeyHash", apiKeyHash);
            return jdbc.query(sql, params, TENANT_MAPPER).stream().findFirst();
        }

        @Override
        public Optional<Tenant> findById(TenantId id) {
            var sql = """
                    SELECT id, name, api_key_hash, status, memory_opt_out
                    FROM tenants
                    WHERE id = :id
                    """;
            var params = new MapSqlParameterSource("id", id.value());
            return jdbc.query(sql, params, TENANT_MAPPER).stream().findFirst();
        }

        @Override
        public void save(Tenant tenant) {
            var sql = """
                    INSERT INTO tenants (id, name, api_key_hash, status, memory_opt_out)
                    VALUES (:id, :name, :apiKeyHash, :status, :memoryOptOut)
                    ON CONFLICT (id) DO UPDATE SET
                        name = EXCLUDED.name,
                        status = EXCLUDED.status,
                        memory_opt_out = EXCLUDED.memory_opt_out
                    """;
            var params = new MapSqlParameterSource()
                    .addValue("id", tenant.id().value())
                    .addValue("name", tenant.name())
                    .addValue("apiKeyHash", tenant.apiKeyHash())
                    .addValue("status", tenant.status().name())
                    .addValue("memoryOptOut", tenant.memoryOptOut());
            jdbc.update(sql, params);
        }

        private static final RowMapper<Tenant> TENANT_MAPPER = (rs, rowNum) ->
                Tenant.reconstitute(
                        TenantId.of(rs.getString("id")),
                        rs.getString("name"),
                        rs.getString("api_key_hash"),
                        TenantStatus.valueOf(rs.getString("status")),
                        rs.getBoolean("memory_opt_out")
                );
    }

    private static final class JdbcAgentFeedbackRepository implements AgentFeedbackPort {

        private final NamedParameterJdbcTemplate jdbc;

        private JdbcAgentFeedbackRepository(NamedParameterJdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        @Override
        public void record(AgentFeedback feedback) {
            var sql = """
                    INSERT INTO agent_feedback
                        (id, tenant_id, agent_type, decision_id, original_decision,
                         original_confidence, outcome, outcome_detail, recorded_at)
                    VALUES
                        (:id, :tenantId, :agentType, :decisionId, :originalDecision,
                         :originalConfidence, :outcome, :outcomeDetail, :recordedAt)
                    """;
            var params = new MapSqlParameterSource()
                    .addValue("id", feedback.id())
                    .addValue("tenantId", feedback.tenantId().value())
                    .addValue("agentType", feedback.agentType())
                    .addValue("decisionId", feedback.decisionId())
                    .addValue("originalDecision", feedback.originalDecision())
                    .addValue("originalConfidence", feedback.originalConfidence())
                    .addValue("outcome", feedback.outcome().name())
                    .addValue("outcomeDetail", feedback.outcomeDetail())
                    .addValue("recordedAt", feedback.recordedAt());
            jdbc.update(sql, params);
        }

        @Override
        public List<AgentFeedback> findByAgentType(TenantId tenantId, String agentType, int limit) {
            var sql = """
                    SELECT id, tenant_id, agent_type, decision_id, original_decision,
                           original_confidence, outcome, outcome_detail, recorded_at
                    FROM agent_feedback
                    WHERE tenant_id = :tenantId AND agent_type = :agentType
                    ORDER BY recorded_at DESC
                    LIMIT :limit
                    """;
            var params = new MapSqlParameterSource()
                    .addValue("tenantId", tenantId.value())
                    .addValue("agentType", agentType)
                    .addValue("limit", limit);
            return jdbc.query(sql, params, (rs, rowNum) -> AgentFeedback.create(
                    TenantId.of(rs.getString("tenant_id")),
                    rs.getString("agent_type"),
                    UUID.fromString(rs.getString("decision_id")),
                    rs.getString("original_decision"),
                    rs.getDouble("original_confidence"),
                    DecisionOutcome.valueOf(rs.getString("outcome")),
                    rs.getString("outcome_detail")
            ));
        }

        @Override
        public Map<String, Map<String, Object>> getPerformanceStats(TenantId tenantId) {
            var sql = """
                    SELECT agent_type,
                           COUNT(*) AS total,
                           COUNT(*) FILTER (WHERE outcome = 'CORRECT') AS correct,
                           ROUND(COUNT(*) FILTER (WHERE outcome = 'CORRECT') * 100.0 / NULLIF(COUNT(*), 0), 1) AS accuracy_pct
                    FROM agent_feedback
                    WHERE tenant_id = :tenantId
                    GROUP BY agent_type
                    ORDER BY agent_type
                    """;
            var params = new MapSqlParameterSource("tenantId", tenantId.value());
            var result = new LinkedHashMap<String, Map<String, Object>>();
            jdbc.query(sql, params, (rs, rowNum) -> {
                var stats = Map.<String, Object>of(
                        "total", rs.getLong("total"),
                        "correct", rs.getLong("correct"),
                        "accuracy", rs.getDouble("accuracy_pct")
                );
                result.put(rs.getString("agent_type"), stats);
                return null;
            });
            return result;
        }
    }
}
