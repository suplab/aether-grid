package com.suplab.aether.core.domain;

public enum DecisionOutcome {
    CORRECT,   // agent decision was validated as correct
    INCORRECT, // agent decision was wrong (e.g., BLOCK that shouldn't have been)
    UNKNOWN    // outcome not yet determined
}
