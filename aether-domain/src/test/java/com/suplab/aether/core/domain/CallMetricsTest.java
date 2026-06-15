package com.suplab.aether.core.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallMetricsTest {

    @Test
    void classifiesSuccessCorrectly() {
        var metrics = new CallMetrics(200, 50L, Instant.now());
        assertThat(metrics.isSuccess()).isTrue();
        assertThat(metrics.isClientError()).isFalse();
        assertThat(metrics.isServerError()).isFalse();
    }

    @Test
    void classifiesClientErrorCorrectly() {
        var metrics = new CallMetrics(404, 30L, Instant.now());
        assertThat(metrics.isSuccess()).isFalse();
        assertThat(metrics.isClientError()).isTrue();
        assertThat(metrics.isServerError()).isFalse();
    }

    @Test
    void classifiesServerErrorCorrectly() {
        var metrics = new CallMetrics(503, 100L, Instant.now());
        assertThat(metrics.isSuccess()).isFalse();
        assertThat(metrics.isClientError()).isFalse();
        assertThat(metrics.isServerError()).isTrue();
    }

    @Test
    void rejectsInvalidStatusCode() {
        assertThatThrownBy(() -> new CallMetrics(99, 0L, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CallMetrics(600, 0L, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeLatency() {
        assertThatThrownBy(() -> new CallMetrics(200, -1L, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latency");
    }
}
