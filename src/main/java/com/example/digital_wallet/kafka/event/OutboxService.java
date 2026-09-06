package com.example.digital_wallet.kafka.event;


import com.example.digital_wallet.transaction.entity.TransactionType;
import com.example.digital_wallet.transaction.entity.TransferTransaction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class OutboxService {
    OutboxEventRepository outboxEventRepository;
    ObjectMapper objectMapper;




    public void saveTransferCompletedEvent(
            TransferTransaction transaction,
            TransferCompletedEvent event
    ) {


        try {

            String payload =
                    objectMapper.writeValueAsString(event);



            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .transactionType(TransactionType.TRANSFER)
                            .transactionId(transaction.getId())
                            .eventType(EventType.TRANSFER_COMPLETED)
                            .payload(payload)
                            .status(OutboxStatus.PENDING)
                            .createdAt(OffsetDateTime.now())
                            .build();

            outboxEventRepository.save(outboxEvent);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot serialize outbox event",
                    e
            );
        }
    }
}
