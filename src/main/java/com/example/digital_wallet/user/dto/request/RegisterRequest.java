package com.example.digital_wallet.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterRequest(
        @NotBlank
        @Size(min = 6, max = 50)
        String username,

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        String password

) {
}
