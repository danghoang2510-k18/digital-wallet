package com.example.digital_wallet.user.dto.response;

import com.example.digital_wallet.wallet.entity.WalletStatus;


import java.math.BigDecimal;

public record WalletResponse(

        BigDecimal balance,


        String currency,


        WalletStatus status

) {
}
