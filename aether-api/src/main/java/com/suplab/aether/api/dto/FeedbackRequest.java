package com.suplab.aether.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FeedbackRequest(
        @NotNull UUID decisionId,
        @NotBlank String agentType,
        @NotBlank String originalDecision,
        @DecimalMin("0.0") @DecimalMax("1.0") double originalConfidence,
        @NotBlank String outcome,
        String outcomeDetail
) {}
