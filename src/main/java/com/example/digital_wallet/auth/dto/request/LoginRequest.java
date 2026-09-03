package com.example.digital_wallet.auth.dto.request;

public record LoginRequest(
        String username,
        String password
) {
}
