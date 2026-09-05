package com.example.digital_wallet.kafka.consumer;

import com.example.digital_wallet.kafka.event.TransferCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventConsumer {
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "wallet.transaction.completed",
            groupId = "digital-wallet-kafka"
    )
    public void consumeTransferCompleted(
            String payload
    ) {
        try {

            TransferCompletedEvent event =
                    objectMapper.readValue(
                            payload,
                            TransferCompletedEvent.class
                    );

            log.info(
                    "Transfer completed: transactionId={}, amount={}",
                    event.getTransactionId(),
                    event.getAmount()
            );

        } catch (Exception e) {

            log.error(
                    "Cannot deserialize TransferCompletedEvent: {}",
                    payload,
                    e
            );

            throw new IllegalStateException(
                    "Invalid transfer event payload",
                    e
            );
        }
    }
}
