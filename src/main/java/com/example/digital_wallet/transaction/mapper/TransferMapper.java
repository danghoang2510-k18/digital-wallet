package com.example.digital_wallet.transaction.mapper;



import com.example.digital_wallet.transaction.dto.response.TransferResponse;
import com.example.digital_wallet.transaction.entity.TransferTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferMapper {


    @Mapping(target = "senderBalance",source = "transaction.senderWallet.balance")
    @Mapping(target = "receiverUsername",source = "transaction.receiverWallet.user.username")
    @Mapping(target = "transactionId",source = "transaction.id")
    TransferResponse toTransferResponse(TransferTransaction transaction);
}
