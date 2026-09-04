package com.example.digital_wallet.transaction.controller;


import com.example.digital_wallet.common.response.ApiResponse;
import com.example.digital_wallet.transaction.dto.request.TransferRequest;
import com.example.digital_wallet.transaction.dto.response.TransferResponse;
import com.example.digital_wallet.transaction.service.TransferService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequestMapping("/transfer")
public class TransferController {

    TransferService transferService;

    @PostMapping
    public ApiResponse<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKeys
    ) throws InterruptedException {

        return ApiResponse.<TransferResponse>builder()
                .result(
                        transferService.transfer(request,idempotencyKeys)
                )
                .build();
    }
}
