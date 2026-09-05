package com.example.digital_wallet.kafka.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferCompletedEvent {
    UUID eventId;
    UUID transactionId;
    UUID senderWalletId;
    UUID receiverWalletId;
    BigDecimal amount;
    OffsetDateTime occurredAt;
}
