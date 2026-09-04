package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.redis.service.IdempotencyService;
import com.example.digital_wallet.transaction.dto.request.TopUpRequest;
import com.example.digital_wallet.transaction.dto.response.TopUpResponse;
import com.example.digital_wallet.transaction.entity.*;
import com.example.digital_wallet.transaction.mapper.TopUpMapper;
import com.example.digital_wallet.transaction.repository.LedgerEntryRepository;
import com.example.digital_wallet.transaction.repository.TopUpRepository;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.user.repository.UserRepository;
import com.example.digital_wallet.wallet.entity.Wallet;
import com.example.digital_wallet.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TopUpService {

    UserRepository userRepository;
    WalletRepository walletRepository;
    LedgerEntryRepository ledgerRepository;
    TopUpRepository topUpRepository;
    TopUpMapper topUpMapper;

    IdempotencyService idempotencyService;

    @Transactional
    public TopUpResponse topUp(TopUpRequest request,String idempotencyKey
    ) throws InterruptedException {

        boolean acquired  = idempotencyService.tryAccquire(idempotencyKey);
        if(!acquired)
        {

            String value = idempotencyService.getValue(idempotencyKey);

            if("PROCESSING".equals(value))
            {
                throw new AppException(ErrorCode.TRANSACTION_PROCESSING);
            }
            else {
                Optional<TopUpTransaction> existingTransaction =
                        topUpRepository.findByIdempotencyKey(idempotencyKey);

                if (existingTransaction.isPresent()) {
                    return topUpMapper.toTopUpResponse(existingTransaction.get());
                }
            }

        }



        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_EXISTED));

        Wallet wallet = walletRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.WALLET_NOT_EXISTED));

        BigDecimal amount = request.getAmount();

        BigDecimal oldBalance = wallet.getBalance();



        BigDecimal newBalance = oldBalance.add(amount);

        TopUpTransaction transaction = TopUpTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .status(TopUpStatus.SUCCESS)
                .idempotencyKey(idempotencyKey)
                .build();

        topUpRepository.save(transaction);

        wallet.setBalance(newBalance);

        walletRepository.save(wallet);

        LedgerEntry ledger = LedgerEntry.builder()
                .wallet(wallet)
                .type(LedgerType.TOP_UP)
                .amount(amount)
                .balanceBefore(oldBalance)
                .balanceAfter(newBalance)
                .referenceId(transaction.getId())
                .build();

        ledgerRepository.save(ledger);

        idempotencyService.markCompleted(
                idempotencyKey,
                transaction.getId().toString()
        );

        return TopUpResponse.builder()
                .amount(amount)
                .balance(newBalance)
                .build();
    }

}
