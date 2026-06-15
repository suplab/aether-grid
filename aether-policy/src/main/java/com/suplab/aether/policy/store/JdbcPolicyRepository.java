package com.suplab.aether.policy.store;

import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class JdbcPolicyRepository implements PolicyRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcPolicyRepository.class);

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcPolicyRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<String> findActiveYamlByTenant(TenantId tenantId) {
        var sql = """
                SELECT yaml_content
                FROM policies
                WHERE tenant_id = :tenantId AND status = 'ACTIVE'
                LIMIT 1
                """;
        var params = new MapSqlParameterSource("tenantId", tenantId.value());
        return jdbc.query(sql, params, (rs, rowNum) -> rs.getString("yaml_content"))
                .stream().findFirst();
    }

    @Override
    public void savePolicy(TenantId tenantId, UUID policyId, String yamlContent, String changedBy) {
        var insertSql = """
                INSERT INTO policies (id, tenant_id, name, status, yaml_content, created_at, updated_at)
                VALUES (:id, :tenantId, :name, 'DRAFT', :yaml, :now, :now)
                ON CONFLICT (id) DO UPDATE SET
                    yaml_content = EXCLUDED.yaml_content,
                    updated_at = EXCLUDED.updated_at
                """;
        var now = Instant.now();
        var params = new MapSqlParameterSource()
                .addValue("id", policyId)
                .addValue("tenantId", tenantId.value())
                .addValue("name", "policy-" + policyId.toString().substring(0, 8))
                .addValue("yaml", yamlContent)
                .addValue("now", now);
        jdbc.update(insertSql, params);

        var versionSql = """
                INSERT INTO policy_versions (id, policy_id, tenant_id, version, yaml_content, changed_by, created_at)
                SELECT :versionId, :policyId, :tenantId,
                       COALESCE(MAX(version), 0) + 1,
                       :yaml, :changedBy, :now
                FROM policy_versions
                WHERE policy_id = :policyId
                """;
        var versionParams = new MapSqlParameterSource()
                .addValue("versionId", UUID.randomUUID())
                .addValue("policyId", policyId)
                .addValue("tenantId", tenantId.value())
                .addValue("yaml", yamlContent)
                .addValue("changedBy", changedBy)
                .addValue("now", now);
        jdbc.update(versionSql, versionParams);
        log.info("Saved policy id={} tenant={} by={}", policyId, tenantId, changedBy);
    }

    @Override
    public void activatePolicy(TenantId tenantId, UUID policyId) {
        var supersedeSql = """
                UPDATE policies
                SET status = 'SUPERSEDED'
                WHERE tenant_id = :tenantId AND status = 'ACTIVE'
                """;
        jdbc.update(supersedeSql, new MapSqlParameterSource("tenantId", tenantId.value()));

        var activateSql = """
                UPDATE policies
                SET status = 'ACTIVE', updated_at = :now
                WHERE id = :id AND tenant_id = :tenantId
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", policyId)
                .addValue("tenantId", tenantId.value())
                .addValue("now", Instant.now());
        jdbc.update(activateSql, params);
        log.info("Activated policy id={} tenant={}", policyId, tenantId);
    }

    @Override
    public void archivePolicy(TenantId tenantId, UUID policyId) {
        var sql = """
                UPDATE policies
                SET status = 'ARCHIVED', updated_at = :now
                WHERE id = :id AND tenant_id = :tenantId
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", policyId)
                .addValue("tenantId", tenantId.value())
                .addValue("now", Instant.now());
        jdbc.update(sql, params);
    }
}
