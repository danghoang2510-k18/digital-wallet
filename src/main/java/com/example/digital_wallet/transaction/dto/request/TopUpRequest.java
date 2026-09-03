package com.example.digital_wallet.transaction.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TopUpRequest {

    @NotNull
    @DecimalMin(value = "10000")
    private BigDecimal amount;
}
