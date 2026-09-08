package com.example.digital_wallet.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenPair {

    private String accessToken;

    private String refreshToken;
}
