package com.suplab.aether.agents.bridge;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aether.core")
public record AetherCoreProperties(
        String baseUrl,
        String apiKey,
        int connectTimeoutMs,
        int readTimeoutMs
) {
    public AetherCoreProperties {
        connectTimeoutMs = connectTimeoutMs > 0 ? connectTimeoutMs : 3000;
        readTimeoutMs = readTimeoutMs > 0 ? readTimeoutMs : 5000;
    }
}
