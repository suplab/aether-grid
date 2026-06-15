package com.suplab.aether.policy.gdpr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GdprRedactionServiceTest {

    private GdprRedactionService service;

    @BeforeEach
    void setUp() {
        service = new GdprRedactionService();
    }

    @Test
    void redacts_email_addresses() {
        var result = service.redact("Contact us at support@example.com for help");
        assertThat(result).doesNotContain("support@example.com");
        assertThat(result).contains("[REDACTED]");
    }

    @Test
    void redacts_credit_card_numbers() {
        var result = service.redact("Payment card: 4111111111111111");
        assertThat(result).doesNotContain("4111111111111111");
        assertThat(result).contains("[REDACTED]");
    }

    @Test
    void redacts_ssn() {
        var result = service.redact("SSN: 123-45-6789");
        assertThat(result).doesNotContain("123-45-6789");
        assertThat(result).contains("[REDACTED]");
    }

    @Test
    void redacts_jwt_bearer_token() {
        var result = service.redact("Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.signature");
        assertThat(result).doesNotContain("eyJhbGciOiJIUzI1NiJ9");
        assertThat(result).contains("[REDACTED]");
    }

    @Test
    void passesThrough_clean_text() {
        var clean = "GET /v1/products responded 200 in 45ms";
        assertThat(service.redact(clean)).isEqualTo(clean);
    }

    @Test
    void containsPii_detectsEmail() {
        assertThat(service.containsPii("user@test.com")).isTrue();
        assertThat(service.containsPii("clean text without pii")).isFalse();
    }

    @Test
    void handles_null_input_gracefully() {
        assertThat(service.redact(null)).isNull();
        assertThat(service.redact("")).isEmpty();
        assertThat(service.containsPii(null)).isFalse();
    }

    @Test
    void redacts_multiple_pii_in_same_string() {
        var input = "Email: user@example.com, SSN: 123-45-6789";
        var result = service.redact(input);
        assertThat(result).doesNotContain("user@example.com");
        assertThat(result).doesNotContain("123-45-6789");
    }
}
