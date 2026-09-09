package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.common.security.CurrentUserService;
import com.example.digital_wallet.kafka.event.OutboxService;
import com.example.digital_wallet.kafka.event.TopUpCompletedEvent;
import com.example.digital_wallet.kafka.event.TransactionCompletedEvent;
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


import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

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
    ) {

        User user = currentUserService.getCurrentUser();

        boolean acquired  = idempotencyService.tryAcquire(idempotencyKey,user.getId(),TransactionType.TOP_UP);
        if(!acquired)
        {

            String value = idempotencyService.getValue(idempotencyKey,user.getId(),TransactionType.TOP_UP);

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



        try {

            Wallet wallet = walletService.getWalletByUserId(user.getId());

            BigDecimal amount = request.getAmount();

            BigDecimal oldBalance = wallet.getBalance();


            BigDecimal newBalance = oldBalance.add(amount);

            TopUpTransaction transaction = TopUpTransaction.builder()
                    .wallet(wallet)
                    .amount(amount)
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey(idempotencyKey)
                    .build();

            transaction = topUpRepository.save(transaction);

            wallet.setBalance(newBalance);

            TransactionCompletedEvent event =
                    TopUpCompletedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .transactionId(transaction.getId())
                            .transactionType(TransactionType.TRANSFER)
                            .amount(amount)
                            .occurredAt(OffsetDateTime.now())
                            .walletId(wallet.getId())
                            .build();


            ledgerService.create(
                    wallet,
                    LedgerType.TOP_UP,
                    amount,
                    oldBalance,
                    newBalance
                    , transaction.getId()
            );

            outboxService.saveTransferCompletedEvent(
                    transaction,
                    event
            );

            idempotencyService.markCompleted(
                    idempotencyKey,
                    user.getId(),
                    TransactionType.TOP_UP,
                    transaction.getId().toString()
            );

            return TopUpResponse.builder()
                    .amount(transaction.getAmount())
                    .balance(newBalance)
                    .status(transaction.getStatus())
                    .build();
        }
        catch (Exception ex)
        {
            idempotencyService.removeValue(idempotencyKey
                    ,user.getId()
                    ,TransactionType.TOP_UP);

            throw ex;
        }
    }

}
