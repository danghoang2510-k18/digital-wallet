package com.example.digital_wallet.auth.controller;


import com.example.digital_wallet.auth.dto.request.IntrospectRequest;
import com.example.digital_wallet.auth.dto.request.LoginRequest;
import com.example.digital_wallet.auth.dto.response.AuthenticationResponse;
import com.example.digital_wallet.auth.dto.response.IntrospectResponse;
import com.example.digital_wallet.auth.service.AuthenticationService;
import com.example.digital_wallet.common.response.ApiResponse;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequestMapping("/auth")
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> handleLogin(@RequestBody LoginRequest request,

                                                           HttpServletResponse response) throws ParseException, JOSEException {
        var result = authenticationService.authentication(request,response);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }


    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> handleVerify(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspectResponse(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(HttpServletRequest request,
                                                            HttpServletResponse response) throws ParseException, JOSEException {
        var result = authenticationService.handleRefreshToken(request,response);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)

                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<AuthenticationResponse> logout(HttpServletResponse response,HttpServletRequest request) throws ParseException, JOSEException {
        authenticationService.handleLogout(response,request);

        return ApiResponse.<AuthenticationResponse>builder()
                .message("Log out success")
                .build();
    }

}
