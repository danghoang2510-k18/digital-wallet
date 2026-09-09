package com.example.digital_wallet.transaction.dto.response;

import com.example.digital_wallet.transaction.entity.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private UUID transactionId;

    private String receiverUsername;

    private BigDecimal amount;

    private BigDecimal senderBalance;

    private TransactionStatus status;

    private OffsetDateTime createdAt;
}
