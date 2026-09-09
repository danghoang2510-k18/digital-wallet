package com.example.digital_wallet.kafka.event;

import com.example.digital_wallet.transaction.entity.TransactionType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
@SuperBuilder
public class TransferCompletedEvent extends TransactionCompletedEvent{

    private UUID senderWalletId;
    private UUID receiverWalletId;

    public TransferCompletedEvent(UUID eventId, UUID transactionId, TransactionType transactionType, BigDecimal amount, OffsetDateTime occurredAt, UUID senderWalletId, UUID receiverWalletId) {
        super(eventId, transactionId, transactionType, amount, occurredAt);
        this.senderWalletId = senderWalletId;
        this.receiverWalletId = receiverWalletId;
    }
}
