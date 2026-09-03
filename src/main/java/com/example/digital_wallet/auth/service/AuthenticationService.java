package com.example.digital_wallet.auth.service;


import com.example.digital_wallet.auth.dto.request.IntrospectRequest;
import com.example.digital_wallet.auth.dto.request.LoginRequest;
import com.example.digital_wallet.auth.dto.response.AuthenticationResponse;
import com.example.digital_wallet.auth.dto.response.IntrospectResponse;
import com.example.digital_wallet.common.config.CustomJwtDecoder;
import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.user.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationService {

    static String SIGNER_KEY="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE3ODQ4NTc3NzYsImV4cCI6MTc4NDg2MTM3NiwianRpIjoiYzlkODFiZDItN2VlYy00NWY4LTkwZTctYjU3N2ViYTFmZWJkIiwiaXNzIjoiYXBpLmV4YW1wbGUuY29tIiwic3ViIjoidXNlcl81NDcwIiwiYXVkIjoiaHR0cHM6Ly9leGFtcGxlLmNvbSJ9.l55BiDbofHom5RpqdgopX8kdd6wfKsUGaT5SD1PUJWW0wafvKNbWL7yN5YsVkzbyHy55W_ETPDowpk-cWvxXGw";

    UserRepository userRepository;

    PasswordEncoder passwordEncoder;



    public String generateToken(User user){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);


        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("hd2005.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1,ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope",buildScope(user))
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);


        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
                    return jwsObject.serialize();
        }
        catch (Exception e)
        {
            throw  new RuntimeException(e.getMessage());
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

    public IntrospectResponse introspectResponse(IntrospectRequest request) throws ParseException, JOSEException {
        String token = request.getToken();


        boolean isValid = true;
        try {
            verifyToken(token);
        }
        catch (AppException e)
        {
            isValid = false;
        }



        return IntrospectResponse.builder()
                .valid(isValid)
                .build();

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



    public AuthenticationResponse authentication(LoginRequest request)
    {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_EXISTED));

        if(!passwordEncoder.matches(request.password(), user.getPassword()))
        {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .build();
    }
}
