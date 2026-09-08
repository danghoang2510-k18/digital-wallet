package com.example.digital_wallet.kafka.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500000)
    public void publishEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findByStatusOrderByCreatedAtAsc(
                                OutboxStatus.PENDING
                        );

        for (OutboxEvent event : events) {
            publish(event);
        }
    }

    private void publish(OutboxEvent event) {

        kafkaTemplate.send(
                getTopic(event.getEventType().toString()),
                event.getTransactionId().toString(),
                event.getPayload()
        ).whenComplete((result, ex) -> {

            if (ex == null) {

                event.setStatus(
                        OutboxStatus.PUBLISHED
                );

                event.setPublishedAt(
                        OffsetDateTime.now()
                );

                outboxEventRepository.save(event);

            } else {

                log.error(
                        "Failed to publish outbox event {}",
                        event.getId(),
                        ex
                );
            }
        });
    }

    private String getTopic(String eventType) {

        return switch (eventType) {
            case "TRANSFER_COMPLETED" ->
                    "wallet.transaction.completed";

            default ->
                    throw new IllegalArgumentException(
                            "Unknown event type: " + eventType
                    );
        };
    }
}