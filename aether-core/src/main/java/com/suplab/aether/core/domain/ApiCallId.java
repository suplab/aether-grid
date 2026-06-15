package com.suplab.aether.core.domain;

import java.util.UUID;

public record ApiCallId(UUID value) {

    public ApiCallId {
        if (value == null) throw new IllegalArgumentException("ApiCallId value must not be null");
    }

    public static ApiCallId generate() {
        return new ApiCallId(UUID.randomUUID());
    }

    public static ApiCallId of(String value) {
        return new ApiCallId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
