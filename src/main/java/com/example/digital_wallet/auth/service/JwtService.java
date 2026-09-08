package com.example.digital_wallet.auth.service;

import com.example.digital_wallet.auth.dto.response.TokenPair;
import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.redis.service.RefreshTokenRedisService;
import com.example.digital_wallet.user.entity.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtService {

    @Value("${jwt.signerKey}")
    String SIGNER_KEY;

    @Value("${jwt.token.access-time}")
    long accessToken;

    @Value("${jwt.token.refresh-time}")
    long refreshToken;



    static JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

    public TokenPair generateTokens(User user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String generateAccessToken(User user) {


        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("hd2005.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now()
                                .plus(accessToken, ChronoUnit.MINUTES)
                                .toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "access")
                .claim("scope", buildScope(user))
                .build();

        return signToken(header, claimsSet);
    }

    private String generateRefreshToken(User user) {


        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("hd2005.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now()
                                .plus(refreshToken, ChronoUnit.DAYS)
                                .toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .build();

        return signToken(header, claimsSet);
    }

    private String signToken(JWSHeader header, JWTClaimsSet claimsSet) {
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Cannot generate JWT token", e);
        }
    }


    private String buildScope(User user)
    {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!user.getRoles().isEmpty())
        {
            user.getRoles().forEach(role ->
                    {
                        stringJoiner.add("ROLE_"+role.getName());
                        if(!role.getPermissions().isEmpty())
                        {
                            role.getPermissions().forEach(permission ->
                                    {
                                        stringJoiner.add(permission.getName());
                                    }
                            );
                        }

                    }
            );
        }
        return stringJoiner.toString();
    }


    public SignedJWT verifyToken(String token) throws ParseException, JOSEException {

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());



        SignedJWT signedJWT = SignedJWT.parse(token);

        var verified = signedJWT.verify(verifier);

//        Kiểm tra token có hết hạn hay chưa
        Date expityTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if(!(verified && expityTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

//        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
//            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    public String extractJti(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(token);

        return signedJWT.getJWTClaimsSet().getJWTID();
    }


}
