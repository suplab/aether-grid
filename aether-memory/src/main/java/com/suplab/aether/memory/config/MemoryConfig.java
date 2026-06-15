package com.suplab.aether.memory.config;

import com.suplab.aether.core.ports.EmbeddingPort;
import com.suplab.aether.core.ports.MemoryStore;
import com.suplab.aether.core.ports.TenantRepository;
import com.suplab.aether.memory.consumer.ApiCallMemoryConsumer;
import com.suplab.aether.policy.gdpr.GdprRedactionService;
import com.suplab.aether.memory.embedding.OllamaEmbeddingService;
import com.suplab.aether.memory.lifecycle.MemoryLifecycleService;
import com.suplab.aether.memory.store.PGVectorMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class MemoryConfig {

    @Bean
    public EmbeddingPort embeddingPort(
            @Value("${aether.memory.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${aether.memory.ollama.model:all-minilm}") String model
    ) {
        return new OllamaEmbeddingService(baseUrl, model);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager txManager) {
        return new TransactionTemplate(txManager);
    }

    @Bean
    public MemoryStore memoryStore(NamedParameterJdbcTemplate jdbc, TransactionTemplate tx) {
        return new PGVectorMemoryStore(jdbc, tx);
    }

    @Bean
    public MemoryLifecycleService memoryLifecycleService(NamedParameterJdbcTemplate jdbc) {
        return new MemoryLifecycleService(jdbc);
    }

    @Bean
    public ApiCallMemoryConsumer apiCallMemoryConsumer(EmbeddingPort embeddingPort, MemoryStore memoryStore,
                                                        TenantRepository tenantRepository,
                                                        GdprRedactionService gdprRedactionService) {
        return new ApiCallMemoryConsumer(embeddingPort, memoryStore, tenantRepository, gdprRedactionService);
    }
}
