package com.example.digital_wallet.user.mapper;


import com.example.digital_wallet.user.dto.request.RegisterRequest;
import com.example.digital_wallet.user.dto.request.UserUpdateRequest;
import com.example.digital_wallet.user.dto.response.UserResponse;
import com.example.digital_wallet.user.dto.response.WalletResponse;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.wallet.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(RegisterRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "walletResponse", source = "wallet")
    UserResponse toUserResponse(User user, Wallet wallet);


    WalletResponse toWalletResponse(Wallet wallet);


    @Mapping(target = "roles",ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
