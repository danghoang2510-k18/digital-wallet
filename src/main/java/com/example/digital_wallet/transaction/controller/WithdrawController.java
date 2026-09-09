package com.example.digital_wallet.transaction.controller;

import com.example.digital_wallet.common.response.ApiResponse;
import com.example.digital_wallet.transaction.dto.request.WithdrawRequest;
import com.example.digital_wallet.transaction.dto.response.WithdrawResponse;
import com.example.digital_wallet.transaction.service.WithdrawService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/withdraw")
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawService withdrawService;

    @PostMapping
    public ApiResponse<WithdrawResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {

        WithdrawResponse result =
                withdrawService.withdraw(
                        request,
                        idempotencyKey
                );

        return ApiResponse.<WithdrawResponse>builder()
                .result(result)
                .build();
    }
}
