package com.suplab.aether.memory.store;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PGVectorMemoryStoreTest {

    @Test
    void toVectorString_producesCorrectFormat() {
        var embedding = new float[]{0.1f, 0.2f, 0.3f};
        var result = PGVectorMemoryStore.toVectorString(embedding);
        assertThat(result).isEqualTo("[0.1,0.2,0.3]");
    }

    @Test
    void parseVectorString_roundTrips() {
        var original = new float[]{1.0f, 0.5f, -0.25f, 0.0f};
        var vectorStr = PGVectorMemoryStore.toVectorString(original);
        var parsed = PGVectorMemoryStore.parseVectorString(vectorStr);

        assertThat(parsed).hasSize(original.length);
        for (int i = 0; i < original.length; i++) {
            assertThat(parsed[i]).isCloseTo(original[i], org.assertj.core.data.Offset.offset(0.0001f));
        }
    }

    @Test
    void toVectorString_handles384Dimensions() {
        var embedding = new float[384];
        for (int i = 0; i < 384; i++) embedding[i] = i * 0.001f;
        var result = PGVectorMemoryStore.toVectorString(embedding);
        assertThat(result).startsWith("[0.0,").endsWith("]");
        assertThat(PGVectorMemoryStore.parseVectorString(result)).hasSize(384);
    }

    @Test
    void parseVectorString_handlesNullGracefully() {
        assertThat(PGVectorMemoryStore.parseVectorString(null)).isEmpty();
        assertThat(PGVectorMemoryStore.parseVectorString("")).isEmpty();
    }
}
