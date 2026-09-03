package com.example.digital_wallet.transaction.controller;


import com.example.digital_wallet.common.response.ApiResponse;
import com.example.digital_wallet.transaction.dto.request.TopUpRequest;
import com.example.digital_wallet.transaction.dto.request.TransferRequest;
import com.example.digital_wallet.transaction.dto.response.TopUpResponse;
import com.example.digital_wallet.transaction.dto.response.TransactionHistoryResponse;
import com.example.digital_wallet.transaction.dto.response.TransferResponse;
import com.example.digital_wallet.transaction.entity.LedgerType;
import com.example.digital_wallet.transaction.service.TopUpService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

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

    @RequestMapping(method = RequestMethod.GET,value = "/transaction-history")
    public ApiResponse<Page<TransactionHistoryResponse>> getAllTransaction(
            @RequestParam(defaultValue = "0",required = false) int page,
            @RequestParam(defaultValue = "5",required = false) int size,
            @RequestParam (defaultValue = "createdAt",required = false)String orderBy,
            @RequestParam(required = false) LedgerType type,
            @RequestParam(required = false)OffsetDateTime from,
            @RequestParam(required = false)OffsetDateTime to
    )
    {
        return ApiResponse.<Page<TransactionHistoryResponse>>builder()
                .result(topUpService.accessTransactionHistory(page,size,orderBy,type,from,to))
                .build();
    }
}

