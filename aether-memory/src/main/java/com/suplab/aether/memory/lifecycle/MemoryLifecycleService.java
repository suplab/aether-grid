package com.suplab.aether.memory.lifecycle;

import com.suplab.aether.memory.store.PGVectorMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

public class MemoryLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(MemoryLifecycleService.class);

    private final NamedParameterJdbcTemplate jdbc;

    public MemoryLifecycleService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Scheduled(cron = "${aether.memory.decay-cron:0 0 3 * * *}")
    public void applyDecay() {
        log.info("Memory lifecycle: starting daily decay pass");
        int decayed = decayIdleMemories(7);
        int purged = purgeWeakMemories(0.01);
        log.info("Memory lifecycle: decayed={} purged={}", decayed, purged);
    }

    private int decayIdleMemories(int idleDays) {
        var sql = """
                UPDATE memory_embeddings
                SET strength = strength * 0.95
                WHERE last_accessed_at < NOW() - (:idleDays * INTERVAL '1 day')
                  AND strength > 0.01
                """;
        return jdbc.update(sql, new MapSqlParameterSource("idleDays", idleDays));
    }

    private int purgeWeakMemories(double strengthThreshold) {
        var sql = """
                DELETE FROM memory_embeddings
                WHERE strength < :threshold
                """;
        return jdbc.update(sql, new MapSqlParameterSource("threshold", strengthThreshold));
    }

    @Scheduled(cron = "${aether.memory.compaction-cron:0 0 4 * * SUN}")
    public void compact() {
        log.info("Memory lifecycle: starting weekly compaction pass");
        int purged = purgeWeakMemories(0.05);
        log.info("Memory lifecycle: compaction removed {} records", purged);
    }
}
