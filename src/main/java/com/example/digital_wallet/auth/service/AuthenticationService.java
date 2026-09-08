package com.example.digital_wallet.auth.service;


import com.example.digital_wallet.auth.dto.request.IntrospectRequest;
import com.example.digital_wallet.auth.dto.request.LoginRequest;
import com.example.digital_wallet.auth.dto.response.AuthenticationResponse;
import com.example.digital_wallet.auth.dto.response.IntrospectResponse;
import com.example.digital_wallet.auth.dto.response.TokenPair;
import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.redis.service.RefreshTokenCookieService;
import com.example.digital_wallet.redis.service.RefreshTokenRedisService;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.user.repository.UserRepository;
import com.example.digital_wallet.user.service.UserService;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.text.ParseException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationService {



    @Value("${jwt.token.access-time}")
    @NonFinal
    long accessToken;

    @Value("${jwt.token.refresh-time}")
    @NonFinal
    long refreshToken;

    UserRepository userRepository;

    PasswordEncoder passwordEncoder;

    JwtService jwtService;

    RefreshTokenRedisService tokenRedisService;

    RefreshTokenCookieService refreshTokenCookieService;

    UserService userService;





    public IntrospectResponse introspectResponse(IntrospectRequest request) throws ParseException, JOSEException {
        String token = request.getToken();


        boolean isValid = true;
        try {
            jwtService.verifyToken(token);
        }
        catch (AppException e)
        {
            isValid = false;
        }



        return IntrospectResponse.builder()
                .valid(isValid)
                .build();

    }





    public AuthenticationResponse authentication(LoginRequest request,
                                                 HttpServletResponse response
                                                 ) throws ParseException, JOSEException {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_EXISTED));

        if(!passwordEncoder.matches(request.password(), user.getPassword()))
        {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }
        return issueTokens(user,response);
    }

    public AuthenticationResponse handleRefreshToken(HttpServletRequest request,
                                                     HttpServletResponse response) throws ParseException, JOSEException {
        Cookie refreshCookie = WebUtils.getCookie(
                request,
                "refresh_token"
        );

        if (refreshCookie == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String refreshToken = refreshCookie.getValue();

        SignedJWT signedJWT = jwtService.verifyToken(refreshToken);

        String tokenType =
                signedJWT.getJWTClaimsSet()
                        .getStringClaim("type");

        if (!"refresh".equals(tokenType)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        String username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userService.getUserByUserName(username);

        if(!tokenRedisService.exists(jwtId))
        {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        tokenRedisService.revoke(jwtId);


        return issueTokens(user,response);


    }

    public AuthenticationResponse issueTokens(User user , HttpServletResponse response) throws ParseException, JOSEException {
        TokenPair tokens = jwtService.generateTokens(user);

        String refreshJti =
                jwtService.extractJti(tokens.getRefreshToken());

        tokenRedisService.save(
                refreshJti,
                user.getId(),
                Duration.ofDays(refreshToken)
        );

        refreshTokenCookieService.set(response,tokens.getRefreshToken(),refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(tokens.getAccessToken())
                .expireTime(accessToken)
                .build();
    }

    public void handleLogout(HttpServletResponse response, HttpServletRequest request) throws ParseException, JOSEException {
        Cookie refreshCookie = WebUtils.getCookie(
                request,
                "refresh_token"
        );

        if (refreshCookie == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String refreshToken = refreshCookie.getValue();

        SignedJWT signedJWT = jwtService.verifyToken(refreshToken);

        String tokenType =
                signedJWT.getJWTClaimsSet()
                        .getStringClaim("type");

        if (!"refresh".equals(tokenType)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();


        if(!tokenRedisService.exists(jwtId))
        {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        tokenRedisService.revoke(jwtId);

        refreshTokenCookieService.clear(response);

    }
}
