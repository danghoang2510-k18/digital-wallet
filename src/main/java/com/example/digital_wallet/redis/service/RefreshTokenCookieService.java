package com.example.digital_wallet.redis.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieService {

    public void set(
            HttpServletResponse response,
            String refreshToken,
            Long ttl
    ) {
        ResponseCookie cookie = ResponseCookie.from(
                        "refresh_token",
                        refreshToken
                )
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/wallet/auth")
                .maxAge(Duration.ofDays(ttl))
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }

    public void clear(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(
                        "refresh_token",
                        ""
                )
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/wallet/auth")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}