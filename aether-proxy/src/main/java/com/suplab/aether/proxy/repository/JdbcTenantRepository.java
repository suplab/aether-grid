package com.suplab.aether.proxy.repository;

import com.suplab.aether.core.domain.Tenant;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.domain.TenantStatus;
import com.suplab.aether.core.ports.TenantRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Optional;
import java.util.UUID;

public class JdbcTenantRepository implements TenantRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcTenantRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Tenant> findByApiKeyHash(String apiKeyHash) {
        var sql = """
                SELECT id, name, api_key_hash, status
                FROM tenants
                WHERE api_key_hash = :apiKeyHash
                """;
        var params = new MapSqlParameterSource("apiKeyHash", apiKeyHash);
        return jdbc.query(sql, params, TENANT_MAPPER).stream().findFirst();
    }

    @Override
    public Optional<Tenant> findById(TenantId id) {
        var sql = """
                SELECT id, name, api_key_hash, status
                FROM tenants
                WHERE id = :id
                """;
        var params = new MapSqlParameterSource("id", id.value());
        return jdbc.query(sql, params, TENANT_MAPPER).stream().findFirst();
    }

    @Override
    public void save(Tenant tenant) {
        var sql = """
                INSERT INTO tenants (id, name, api_key_hash, status)
                VALUES (:id, :name, :apiKeyHash, :status)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    status = EXCLUDED.status
                """;
        var params = new MapSqlParameterSource()
                .addValue("id", tenant.id().value())
                .addValue("name", tenant.name())
                .addValue("apiKeyHash", tenant.apiKeyHash())
                .addValue("status", tenant.status().name());
        jdbc.update(sql, params);
    }

    private static final RowMapper<Tenant> TENANT_MAPPER = (rs, rowNum) ->
            Tenant.reconstitute(
                    TenantId.of(rs.getString("id")),
                    rs.getString("name"),
                    rs.getString("api_key_hash"),
                    TenantStatus.valueOf(rs.getString("status"))
            );
}
