package com.example.digital_wallet.transaction.mapper;

import com.example.digital_wallet.transaction.dto.response.TopUpResponse;
import com.example.digital_wallet.transaction.dto.response.WithdrawResponse;
import com.example.digital_wallet.transaction.entity.TopUpTransaction;
import com.example.digital_wallet.transaction.entity.WithdrawTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface WithdrawMapper {

    @Mapping(target = "balance",source = "transaction.wallet.balance")
    WithdrawResponse toWithdrawResponse(WithdrawTransaction transaction);
}
