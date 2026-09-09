package com.example.digital_wallet.transaction.dto.response;


import com.example.digital_wallet.transaction.entity.TransactionStatus;
import com.example.digital_wallet.wallet.entity.Wallet;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopUpResponse {

    BigDecimal amount;

    BigDecimal balance;

    TransactionStatus status;
}
