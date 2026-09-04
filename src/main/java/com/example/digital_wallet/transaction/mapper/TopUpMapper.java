package com.example.digital_wallet.transaction.mapper;


import com.example.digital_wallet.transaction.dto.response.TopUpResponse;
import com.example.digital_wallet.transaction.entity.TopUpTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TopUpMapper {


    @Mapping(target = "balance",source = "transaction.wallet.balance")
    TopUpResponse toTopUpResponse(TopUpTransaction transaction);
}
