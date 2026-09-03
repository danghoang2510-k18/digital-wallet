package com.example.digital_wallet.transaction.dto.response;

import com.example.digital_wallet.transaction.entity.LedgerType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionHistoryResponse {

    UUID id;
    LedgerType type;
    BigDecimal amount;
    BigDecimal balanceBefore;
    BigDecimal balanceAfter;
    UUID referenceId;
    OffsetDateTime createdAt;
}
