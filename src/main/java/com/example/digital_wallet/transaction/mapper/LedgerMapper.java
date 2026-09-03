package com.example.digital_wallet.transaction.mapper;


import com.example.digital_wallet.transaction.dto.response.TransactionHistoryResponse;
import com.example.digital_wallet.transaction.entity.LedgerEntry;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LedgerMapper {

    TransactionHistoryResponse toTransactionHistoryResponse(LedgerEntry ledgerEntry);
}
