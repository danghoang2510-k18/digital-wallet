package com.example.digital_wallet.kafka.event;

import com.example.digital_wallet.transaction.entity.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
@SuperBuilder
public class WithdrawCompletedEvent extends TransactionCompletedEvent{

    private UUID walletId;

    public WithdrawCompletedEvent(UUID eventId, UUID transactionId, TransactionType transactionType, BigDecimal amount, OffsetDateTime occurredAt, UUID walletId) {
        super(eventId, transactionId, transactionType, amount, occurredAt);
        this.walletId = walletId;
    }
}
