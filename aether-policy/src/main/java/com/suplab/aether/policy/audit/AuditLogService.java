package com.suplab.aether.policy.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suplab.aether.core.domain.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public AuditLogService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.objectMapper = new ObjectMapper();
    }

    public void log(String tenantId, String entityType, String entityId,
                    String action, String actor, Map<String, Object> detail) {
        var sql = """
                INSERT INTO audit_log (id, tenant_id, entity_type, entity_id, action, actor, detail, occurred_at)
                VALUES (:id, :tenantId::uuid, :entityType, :entityId::uuid, :action, :actor, :detail::jsonb, :occurredAt)
                """;
        try {
            var params = new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID())
                    .addValue("tenantId", tenantId)
                    .addValue("entityType", entityType)
                    .addValue("entityId", entityId)
                    .addValue("action", action)
                    .addValue("actor", actor)
                    .addValue("detail", objectMapper.writeValueAsString(detail))
                    .addValue("occurredAt", Instant.now());
            jdbc.update(sql, params);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise audit detail for action={} entity={}: {}", action, entityId, e.getMessage());
        }
    }

    /**
     * Convenience overload for GDPR and lifecycle events where the entity being acted on is the
     * tenant itself (entity_type = "TENANT", entity_id = tenantId).
     */
    public void log(TenantId tenantId, String action, String detail, String actor) {
        var sql = """
                INSERT INTO audit_log (id, tenant_id, entity_type, entity_id, action, actor, detail, occurred_at)
                VALUES (:id, :tenantId::uuid, :entityType, :entityId::uuid, :action, :actor, :detail::jsonb, :occurredAt)
                """;
        try {
            var params = new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID())
                    .addValue("tenantId", tenantId.value().toString())
                    .addValue("entityType", "TENANT")
                    .addValue("entityId", tenantId.value().toString())
                    .addValue("action", action)
                    .addValue("actor", actor)
                    .addValue("detail", objectMapper.writeValueAsString(Map.of("detail", detail)))
                    .addValue("occurredAt", Instant.now());
            jdbc.update(sql, params);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise audit detail for action={} tenant={}: {}", action, tenantId, e.getMessage());
        }
    }

    /**
     * Returns the most recent audit log entries for a tenant, ordered by occurrence time descending.
     */
    public List<Map<String, Object>> findByTenant(TenantId tenantId, int limit) {
        var sql = """
                SELECT id, tenant_id, entity_type, entity_id, action, actor, detail, occurred_at
                FROM audit_log
                WHERE tenant_id = :tenantId::uuid
                ORDER BY occurred_at DESC
                LIMIT :limit
                """;
        var params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId.value().toString())
                .addValue("limit", limit);
        return jdbc.query(sql, params, (rs, rowNum) -> Map.of(
                "id", rs.getString("id"),
                "tenantId", rs.getString("tenant_id"),
                "entityType", rs.getString("entity_type"),
                "entityId", rs.getString("entity_id") != null ? rs.getString("entity_id") : "",
                "action", rs.getString("action"),
                "actor", rs.getString("actor"),
                "detail", rs.getString("detail") != null ? rs.getString("detail") : "{}",
                "occurredAt", rs.getTimestamp("occurred_at").toInstant().toString()
        ));
    }
}
