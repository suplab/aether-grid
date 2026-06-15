package com.suplab.aether.core.ports;

public interface EmbeddingPort {

    float[] embed(String text);

    default int dimensions() {
        return 384;
    }
}
