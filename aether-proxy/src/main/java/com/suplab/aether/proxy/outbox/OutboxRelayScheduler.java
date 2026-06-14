package com.suplab.aether.proxy.outbox;

import com.suplab.aether.proxy.repository.JdbcOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.UUID;

public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);
    private static final int BATCH_SIZE = 100;

    private final JdbcOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxRelayScheduler(JdbcOutboxRepository outboxRepository,
                                 KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${aether.outbox.relay-interval-ms:5000}")
    public void relay() {
        var rows = outboxRepository.findUnpublished(BATCH_SIZE);
        if (rows.isEmpty()) return;

        var published = rows.stream()
                .filter(row -> sendToKafka(row.topic(), row.aggregateId(), row.payload()))
                .map(JdbcOutboxRepository.OutboxRow::id)
                .toList();

        if (!published.isEmpty()) {
            outboxRepository.markPublished(published);
            log.debug("Outbox relay: published {} event(s)", published.size());
        }
    }

    private boolean sendToKafka(String topic, String key, String payload) {
        try {
            kafkaTemplate.send(topic, key, payload).get();
            return true;
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka topic={} key={}: {}", topic, key, e.getMessage());
            return false;
        }
    }
}
