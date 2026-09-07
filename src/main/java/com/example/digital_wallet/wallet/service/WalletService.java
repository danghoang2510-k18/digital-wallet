package com.example.digital_wallet.wallet.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.wallet.entity.Wallet;
import com.example.digital_wallet.wallet.entity.WalletStatus;
import com.example.digital_wallet.wallet.repository.WalletRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class WalletService {

    WalletRepository walletRepository;

    public Wallet getWalletByUserId(UUID userId)
    {
        Wallet wallet = walletRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.WALLET_NOT_EXISTED));

        return wallet;
    }

    public BigDecimal debit(
            Wallet wallet,
            BigDecimal amount
    ) {

        BigDecimal oldBalance = wallet.getBalance();

        if (oldBalance.compareTo(amount) < 0) {
            throw new AppException(
                    ErrorCode.INSUFFICIENT_BALANCE
            );
        }

        BigDecimal newBalance =
                oldBalance.subtract(amount);

        wallet.setBalance(newBalance);

        return newBalance;
    }

    public BigDecimal credit(
            Wallet wallet,
            BigDecimal amount
    ) {

        BigDecimal oldBalance = wallet.getBalance();

        BigDecimal newBalance =
                oldBalance.add(amount);

        wallet.setBalance(newBalance);

        return newBalance;
    }

    public void validateWallet(Wallet wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new AppException(ErrorCode.WALLET_NOT_ACTIVE);
        }
    }


    public UUID getUserIdByWalletId(UUID walletId) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.WALLET_NOT_EXISTED)
                );

        return wallet.getUser().getId();
    }
}
