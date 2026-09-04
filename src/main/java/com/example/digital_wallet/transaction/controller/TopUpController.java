package com.example.digital_wallet.transaction.controller;


import com.example.digital_wallet.common.response.ApiResponse;
import com.example.digital_wallet.transaction.dto.request.TopUpRequest;
import com.example.digital_wallet.transaction.dto.response.TopUpResponse;
import com.example.digital_wallet.transaction.service.TopUpService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TopUpController {


    TopUpService topUpService;

    @PostMapping("/top-up")
    public ApiResponse<TopUpResponse> topUp(
            @Valid @RequestBody TopUpRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) throws InterruptedException {
        return ApiResponse.<TopUpResponse>builder()
                .result(topUpService.topUp(request,idempotencyKey))
                .build();
    }





}

