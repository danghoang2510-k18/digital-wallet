package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.common.security.CurrentUserService;
import com.example.digital_wallet.kafka.event.OutboxService;
import com.example.digital_wallet.redis.service.IdempotencyService;
import com.example.digital_wallet.transaction.dto.request.TopUpRequest;
import com.example.digital_wallet.transaction.dto.response.TopUpResponse;
import com.example.digital_wallet.transaction.entity.*;
import com.example.digital_wallet.transaction.mapper.TopUpMapper;

import com.example.digital_wallet.transaction.repository.TopUpRepository;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.wallet.entity.Wallet;
import com.example.digital_wallet.wallet.repository.WalletRepository;
import com.example.digital_wallet.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


import org.springframework.stereotype.Service;

import java.math.BigDecimal;


import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TopUpService {

    WalletRepository walletRepository;
    TopUpRepository topUpRepository;
    TopUpMapper topUpMapper;
    IdempotencyService idempotencyService;
    CurrentUserService currentUserService;
    WalletService walletService;
    LedgerService ledgerService;
    OutboxService outboxService;


    @Transactional
    public TopUpResponse topUp(TopUpRequest request,String idempotencyKey
    ) throws InterruptedException {

        boolean acquired  = idempotencyService.tryAcquire(idempotencyKey);
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



        User user = currentUserService.getCurrentUser();

        Wallet wallet = walletService.getWalletByUserId(user.getId());

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


        ledgerService.create(
                wallet,
                LedgerType.TOP_UP,
                amount,
                oldBalance,
                newBalance
                ,transaction.getId()
                );



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
