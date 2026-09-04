package com.example.digital_wallet.transaction.controller;


import com.example.digital_wallet.common.response.ApiResponse;
import com.example.digital_wallet.transaction.dto.response.TransactionHistoryResponse;
import com.example.digital_wallet.transaction.entity.LedgerType;
import com.example.digital_wallet.transaction.service.TransactionHistoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequestMapping("/transfer")
public class TransactionHistoryController {

    TransactionHistoryService transactionHistoryService;

    @RequestMapping(method = RequestMethod.GET)
    public ApiResponse<Page<TransactionHistoryResponse>> getAllTransaction(
            @RequestParam(defaultValue = "0",required = false) int page,
            @RequestParam(defaultValue = "5",required = false) int size,
            @RequestParam (defaultValue = "createdAt",required = false)String orderBy,
            @RequestParam(required = false) LedgerType type,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false)OffsetDateTime to
    )
    {
        return ApiResponse.<Page<TransactionHistoryResponse>>builder()
                .result(transactionHistoryService.accessTransactionHistory(page,size,orderBy,type,from,to))
                .build();
    }
}
