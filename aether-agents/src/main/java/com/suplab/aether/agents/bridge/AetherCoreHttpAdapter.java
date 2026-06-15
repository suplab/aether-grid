package com.suplab.aether.agents.bridge;

import com.suplab.aether.core.domain.PersonalContext;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.PersonalContextPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "aether.core.base-url")
@EnableConfigurationProperties(AetherCoreProperties.class)
public class AetherCoreHttpAdapter implements PersonalContextPort {

    private static final Logger log = LoggerFactory.getLogger(AetherCoreHttpAdapter.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public AetherCoreHttpAdapter(AetherCoreProperties props) {
        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
                .build();
    }

    @Override
    public Optional<PersonalContext> fetchFor(TenantId tenantId, String userId) {
        try {
            var body = restClient.get()
                    .uri("/api/v1/personal-context/{tenantId}/{userId}", tenantId.value(), userId)
                    .retrieve()
                    .body(MAP_TYPE);

            if (body == null) {
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            var summaries = (List<String>) body.getOrDefault("recentMemorySummaries", List.of());
            @SuppressWarnings("unchecked")
            var prefs = (Map<String, Object>) body.getOrDefault("preferences", Map.of());
            var emotionalState = (String) body.getOrDefault("emotionalState", "NEUTRAL");
            var engagementScore = body.containsKey("engagementScore")
                    ? ((Number) body.get("engagementScore")).doubleValue() : 0.5;

            return Optional.of(new PersonalContext(
                    userId, tenantId, summaries, prefs, emotionalState, engagementScore, Instant.now()));
        } catch (Exception e) {
            log.warn("AetherCore personal context fetch failed for tenant={} user={}: {}",
                    tenantId.value(), userId, e.getMessage());
            return Optional.empty();
        }
    }
}
