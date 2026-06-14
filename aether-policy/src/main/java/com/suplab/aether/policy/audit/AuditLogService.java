package com.suplab.aether.policy.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
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
                INSERT INTO audit_log (id, tenant_id, entity_type, entity_id, action, actor, detail, created_at)
                VALUES (:id, :tenantId, :entityType, :entityId, :action, :actor, :detail::jsonb, :createdAt)
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
                    .addValue("createdAt", Instant.now());
            jdbc.update(sql, params);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise audit detail for action={} entity={}: {}", action, entityId, e.getMessage());
        }
    }
}
