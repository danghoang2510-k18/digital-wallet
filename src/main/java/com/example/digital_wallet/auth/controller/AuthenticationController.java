package com.example.digital_wallet.auth.controller;


import com.example.digital_wallet.auth.dto.request.IntrospectRequest;
import com.example.digital_wallet.auth.dto.request.LoginRequest;
import com.example.digital_wallet.auth.dto.response.AuthenticationResponse;
import com.example.digital_wallet.auth.dto.response.IntrospectResponse;
import com.example.digital_wallet.auth.service.AuthenticationService;
import com.example.digital_wallet.common.response.ApiResponse;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequestMapping("/auth")
public class AuthenticationController {

    AuthenticationService authenticationService;

    @RequestMapping(method = RequestMethod.POST, value = "/login")
    public ApiResponse<AuthenticationResponse> handleLogin(@RequestBody LoginRequest request)
    {
        var result = authenticationService.authentication(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }


    @RequestMapping(method = RequestMethod.POST,value = "/introspect")
    public ApiResponse<IntrospectResponse> handleVerify(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspectResponse(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }
}
