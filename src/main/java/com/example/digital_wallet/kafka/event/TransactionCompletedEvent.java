package com.example.digital_wallet.kafka.event;

import com.example.digital_wallet.transaction.entity.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class TransactionCompletedEvent {
    UUID eventId;
    UUID transactionId;
    TransactionType transactionType;
    BigDecimal amount;
    OffsetDateTime occurredAt;
}
