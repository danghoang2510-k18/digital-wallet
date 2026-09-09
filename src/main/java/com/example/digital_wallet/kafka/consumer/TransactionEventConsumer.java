package com.example.digital_wallet.kafka.consumer;

import com.example.digital_wallet.kafka.event.ProcessedEventRepository;
import com.example.digital_wallet.kafka.event.TransactionCompletedEvent;
import com.example.digital_wallet.kafka.event.TransactionCompletedEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventConsumer {
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;
    private final TransactionCompletedEventHandler transactionCompletedEventHandler;

    @KafkaListener(
            topics = "wallet.transaction.completed",
            groupId = "digital-wallet-kafka"
    )
    public void consumeTransferCompleted(
            String payload
    ) {
        try {

            TransactionCompletedEvent event =
                    objectMapper.readValue(
                            payload,
                            TransactionCompletedEvent.class
                    );

            UUID eventId = event.getEventId();

           transactionCompletedEventHandler.process(event);

        } catch (Exception e) {

            log.error(
                    "Cannot deserialize TransferCompletedEvent: {}",
                    payload,
                    e
            );

            throw new IllegalStateException(
                    "TEST KAFKA ERROR",
                    e
            );
        }
    }


}
