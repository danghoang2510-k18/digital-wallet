package com.example.digital_wallet.transaction.controller;


import com.example.digital_wallet.common.response.ApiResponse;
import com.example.digital_wallet.transaction.dto.request.TopUpRequest;
import com.example.digital_wallet.transaction.dto.request.TransferRequest;
import com.example.digital_wallet.transaction.dto.response.TopUpResponse;
import com.example.digital_wallet.transaction.dto.response.TransferResponse;
import com.example.digital_wallet.transaction.service.TopUpService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TopUpController {


    TopUpService topUpService;
    @PostMapping("/top-up")
    public ApiResponse<TopUpResponse> topUp(
            @Valid @RequestBody TopUpRequest request
    ) throws InterruptedException {
        return ApiResponse.<TopUpResponse>builder()
                .result(topUpService.topUp(request))
                .build();
    }


    @PostMapping("/transfer")
    public ApiResponse<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request) throws InterruptedException {

        return ApiResponse.<TransferResponse>builder()
                .result(
                        topUpService.transfer(request)
                )
                .build();
    }
}

