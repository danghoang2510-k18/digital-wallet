package com.example.digital_wallet.transaction.dto.response;


import com.example.digital_wallet.transaction.entity.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawResponse {

    BigDecimal amount;
    BigDecimal balance;
    TransactionStatus status;
    OffsetDateTime createdAt;
}
