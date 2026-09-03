package com.example.digital_wallet.user.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        String username,
        String email,
        String status,
        WalletResponse walletResponse
) {
}
