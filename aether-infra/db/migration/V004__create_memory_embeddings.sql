-- V004: Memory Embeddings — semantic memory store with pgvector
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE memory_embeddings (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL,
    memory_type   VARCHAR(20)  NOT NULL,
    content       TEXT         NOT NULL,
    embedding     vector(384),
    strength      FLOAT        NOT NULL DEFAULT 1.0,
    api_call_id   UUID,
    last_accessed TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_memory_embeddings PRIMARY KEY (id),
    CONSTRAINT fk_memory_embeddings_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_memory_embeddings_call FOREIGN KEY (api_call_id) REFERENCES api_calls (id) ON DELETE SET NULL,
    CONSTRAINT chk_memory_type CHECK (memory_type IN ('EPISODIC', 'SEMANTIC', 'PROCEDURAL', 'EMOTIONAL')),
    CONSTRAINT chk_memory_strength CHECK (strength >= 0.0 AND strength <= 1.0)
);

-- IVFFlat index for approximate nearest-neighbour cosine similarity search
-- lists=100 is suitable for up to ~1M vectors; increase for larger datasets
CREATE INDEX idx_memory_embeddings_cosine
    ON memory_embeddings USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX idx_memory_embeddings_tenant ON memory_embeddings (tenant_id);
CREATE INDEX idx_memory_embeddings_tenant_type ON memory_embeddings (tenant_id, memory_type);
CREATE INDEX idx_memory_embeddings_strength ON memory_embeddings (tenant_id, strength, last_accessed);

COMMENT ON TABLE memory_embeddings IS 'Semantic memory store: all-MiniLM-L6-v2 384-dim embeddings with pgvector';
COMMENT ON COLUMN memory_embeddings.embedding IS '384-dimensional vector from all-MiniLM-L6-v2 via Ollama';
COMMENT ON COLUMN memory_embeddings.strength IS 'Decay/reinforcement score 0.0-1.0; reinforced on retrieval, decays over time';
