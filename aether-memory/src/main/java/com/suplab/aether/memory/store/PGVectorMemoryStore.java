package com.suplab.aether.memory.store;

import com.suplab.aether.core.domain.MemoryRecord;
import com.suplab.aether.core.domain.MemoryType;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PGVectorMemoryStore implements MemoryStore {

    private static final Logger log = LoggerFactory.getLogger(PGVectorMemoryStore.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate tx;

    public PGVectorMemoryStore(NamedParameterJdbcTemplate jdbc, TransactionTemplate tx) {
        this.jdbc = jdbc;
        this.tx = tx;
    }

    @Override
    public void store(MemoryRecord record) {
        tx.execute(status -> {
            var sql = """
                    INSERT INTO memory_embeddings
                        (id, tenant_id, memory_type, content, embedding, strength, source_call_id, created_at, last_accessed_at)
                    VALUES
                        (:id, :tenantId, :memoryType, :content, :embedding::vector, :strength,
                         :sourceCallId, :createdAt, :lastAccessedAt)
                    ON CONFLICT (id) DO UPDATE SET
                        strength = EXCLUDED.strength,
                        last_accessed_at = EXCLUDED.last_accessed_at
                    """;
            var params = new MapSqlParameterSource()
                    .addValue("id", record.id())
                    .addValue("tenantId", record.tenantId().value())
                    .addValue("memoryType", record.memoryType().name())
                    .addValue("content", record.content())
                    .addValue("embedding", toVectorString(record.embedding()))
                    .addValue("strength", record.strength())
                    .addValue("sourceCallId", record.sourceCallId())
                    .addValue("createdAt", Timestamp.from(record.createdAt()))
                    .addValue("lastAccessedAt", Timestamp.from(record.lastAccessedAt()));
            jdbc.update(sql, params);
            return null;
        });
        log.debug("Stored memory id={} type={} tenant={}",
                record.id(), record.memoryType(), record.tenantId());
    }

    @Override
    public List<MemoryRecord> findSimilar(TenantId tenantId, float[] queryEmbedding, int topK) {
        var sql = """
                SELECT id, tenant_id, memory_type, content,
                       embedding::text AS embedding_text,
                       strength, source_call_id, created_at, last_accessed_at
                FROM memory_embeddings
                WHERE tenant_id = :tenantId
                ORDER BY embedding <=> :queryEmbedding::vector ASC
                LIMIT :topK
                """;
        var params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId.value())
                .addValue("queryEmbedding", toVectorString(queryEmbedding))
                .addValue("topK", topK);

        var results = jdbc.query(sql, params, MEMORY_RECORD_MAPPER);
        if (!results.isEmpty()) {
            updateLastAccessed(tenantId, results.stream().map(MemoryRecord::id).toList());
        }
        return results;
    }

    @Override
    public List<MemoryRecord> findByType(TenantId tenantId, MemoryType type, int limit) {
        var sql = """
                SELECT id, tenant_id, memory_type, content,
                       embedding::text AS embedding_text,
                       strength, source_call_id, created_at, last_accessed_at
                FROM memory_embeddings
                WHERE tenant_id = :tenantId AND memory_type = :memoryType
                ORDER BY strength DESC, last_accessed_at DESC
                LIMIT :limit
                """;
        var params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId.value())
                .addValue("memoryType", type.name())
                .addValue("limit", limit);
        return jdbc.query(sql, params, MEMORY_RECORD_MAPPER);
    }

    @Override
    public void delete(TenantId tenantId, UUID memoryId) {
        var sql = """
                DELETE FROM memory_embeddings
                WHERE id = :id AND tenant_id = :tenantId
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", memoryId)
                .addValue("tenantId", tenantId.value());
        int deleted = jdbc.update(sql, params);
        log.debug("Deleted {} memory record(s) id={} tenant={}", deleted, memoryId, tenantId);
    }

    @Override
    public void deleteAll(TenantId tenantId) {
        var sql = "DELETE FROM memory_embeddings WHERE tenant_id = :tenantId";
        var params = new MapSqlParameterSource("tenantId", tenantId.value());
        int deleted = jdbc.update(sql, params);
        log.info("GDPR erasure: deleted all {} memory record(s) for tenant={}", deleted, tenantId);
    }

    void decayStrengths(int daysThreshold) {
        var sql = """
                UPDATE memory_embeddings
                SET strength = strength * 0.95
                WHERE last_accessed_at < NOW() - INTERVAL ':days days'
                  AND strength > 0.01
                """;
        var params = new MapSqlParameterSource("days", daysThreshold);
        int updated = jdbc.update(sql, params);
        log.info("Memory decay: applied 5% strength reduction to {} records older than {} days", updated, daysThreshold);
    }

    void purgeWeak(double strengthThreshold) {
        var sql = """
                DELETE FROM memory_embeddings
                WHERE strength < :threshold
                """;
        int purged = jdbc.update(sql, new MapSqlParameterSource("threshold", strengthThreshold));
        log.info("Memory compaction: purged {} records with strength < {}", purged, strengthThreshold);
    }

    private void updateLastAccessed(TenantId tenantId, List<UUID> ids) {
        var sql = """
                UPDATE memory_embeddings
                SET last_accessed_at = NOW(),
                    strength = LEAST(1.0, strength * 1.05)
                WHERE id = ANY(:ids::uuid[]) AND tenant_id = :tenantId
                """;
        var params = new MapSqlParameterSource()
                .addValue("ids", ids.stream().map(UUID::toString).toArray(String[]::new))
                .addValue("tenantId", tenantId.value());
        jdbc.update(sql, params);
    }

    static String toVectorString(float[] embedding) {
        var sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(embedding[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    static float[] parseVectorString(String vectorText) {
        if (vectorText == null || vectorText.isEmpty()) return new float[0];
        var inner = vectorText.substring(1, vectorText.length() - 1);
        var parts = inner.split(",");
        var result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }

    private static final RowMapper<MemoryRecord> MEMORY_RECORD_MAPPER = (rs, rowNum) ->
            new MemoryRecord(
                    UUID.fromString(rs.getString("id")),
                    TenantId.of(rs.getString("tenant_id")),
                    MemoryType.valueOf(rs.getString("memory_type")),
                    rs.getString("content"),
                    parseVectorString(rs.getString("embedding_text")),
                    rs.getDouble("strength"),
                    rs.getString("source_call_id") != null
                            ? UUID.fromString(rs.getString("source_call_id")) : null,
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("last_accessed_at").toInstant()
            );
}
