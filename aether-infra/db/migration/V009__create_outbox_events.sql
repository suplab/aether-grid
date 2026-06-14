-- V009: Outbox Events — transactional outbox for reliable Kafka publishing
-- ApiCall rows and their corresponding events are written in ONE transaction.
-- A relay thread reads unpublished rows and publishes to Kafka.
CREATE TABLE outbox_events (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    event_type   VARCHAR(200) NOT NULL,
    aggregate_id UUID        NOT NULL,
    topic        VARCHAR(255) NOT NULL,
    payload      JSONB       NOT NULL,
    published    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP,

    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

-- Partial index: only unpublished rows — this is the relay query hot path
CREATE INDEX idx_outbox_events_unpublished
    ON outbox_events (created_at ASC)
    WHERE published = FALSE;

COMMENT ON TABLE outbox_events IS 'Transactional outbox — events written atomically with their aggregate, published to Kafka by a relay';
COMMENT ON COLUMN outbox_events.topic IS 'Kafka topic name (e.g. aether.api.calls, aether.agent.decisions)';
