package com.suplab.aether.proxy.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JdbcOutboxRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcOutboxRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(String eventType, String aggregateId, String topic, String payloadJson) {
        var sql = """
                INSERT INTO outbox_events (id, event_type, aggregate_id, topic, payload, published, created_at)
                VALUES (:id, :eventType, :aggregateId, :topic, :payload::jsonb, false, :createdAt)
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("eventType", eventType)
                .addValue("aggregateId", aggregateId)
                .addValue("topic", topic)
                .addValue("payload", payloadJson)
                .addValue("createdAt", Instant.now());
        jdbc.update(sql, params);
    }

    public List<OutboxRow> findUnpublished(int limit) {
        var sql = """
                SELECT id, event_type, aggregate_id, topic, payload::text AS payload_text
                FROM outbox_events
                WHERE published = false
                ORDER BY created_at ASC
                LIMIT :limit
                """;
        var params = new MapSqlParameterSource("limit", limit);
        return jdbc.query(sql, params, (rs, rowNum) -> new OutboxRow(
                UUID.fromString(rs.getString("id")),
                rs.getString("event_type"),
                rs.getString("aggregate_id"),
                rs.getString("topic"),
                rs.getString("payload_text")
        ));
    }

    public void markPublished(List<UUID> ids) {
        if (ids.isEmpty()) return;
        var sql = """
                UPDATE outbox_events SET published = true
                WHERE id = ANY(:ids::uuid[])
                """;
        var params = new MapSqlParameterSource("ids",
                ids.stream().map(UUID::toString).toArray(String[]::new));
        jdbc.update(sql, params);
    }

    public record OutboxRow(UUID id, String eventType, String aggregateId, String topic, String payload) {}
}
