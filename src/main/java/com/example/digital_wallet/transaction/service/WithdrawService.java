package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.common.security.CurrentUserService;
import com.example.digital_wallet.kafka.event.OutboxService;
import com.example.digital_wallet.kafka.event.TopUpCompletedEvent;
import com.example.digital_wallet.kafka.event.TransactionCompletedEvent;
import com.example.digital_wallet.kafka.event.WithdrawCompletedEvent;
import com.example.digital_wallet.redis.service.IdempotencyService;
import com.example.digital_wallet.transaction.dto.request.WithdrawRequest;
import com.example.digital_wallet.transaction.dto.response.WithdrawResponse;
import com.example.digital_wallet.transaction.entity.*;
import com.example.digital_wallet.transaction.mapper.WithdrawMapper;
import com.example.digital_wallet.transaction.repository.WithdrawRepository;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.wallet.entity.Wallet;
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
public class WithdrawService {
    WithdrawRepository withdrawRepository;
    WalletService walletService;
    IdempotencyService idempotencyService;
    LedgerService ledgerService;
    CurrentUserService currentUserService;
    OutboxService outboxService;
    WithdrawMapper withdrawMapper;

    @Transactional
    public WithdrawResponse withdraw(
            WithdrawRequest request,
            String idempotencyKey
    ) {

        User user = currentUserService.getCurrentUser();
        boolean acquired = idempotencyService.tryAcquire(idempotencyKey,user.getId(), TransactionType.WITHDRAWAL);

        if(!acquired)
        {

            String value = idempotencyService.getValue(idempotencyKey,user.getId(),TransactionType.TOP_UP);

            if("PROCESSING".equals(value))
            {
                throw new AppException(ErrorCode.TRANSACTION_PROCESSING);
            }
            else {
                Optional<WithdrawTransaction> existingTransaction =
                        withdrawRepository.findByIdempotencyKey(idempotencyKey);

                if (existingTransaction.isPresent()) {
                    return withdrawMapper.toWithdrawResponse(existingTransaction.get());
                }
            }

        }

        try {




            Wallet wallet = walletService.getWalletByUserId(user.getId());


            BigDecimal amount = request.getAmount();




            BigDecimal balanceBefore = wallet.getBalance();

            BigDecimal balanceAfter =
                    walletService.debit(wallet, amount);

            WithdrawTransaction transaction =
                    WithdrawTransaction.builder()
                            .wallet(wallet)
                            .amount(amount)
                            .status(TransactionStatus.SUCCESS)
                            .idempotencyKey(idempotencyKey)
                            .bankAccountNumber(
                                    request.getBankAccountNumber()
                            )
                            .bankCode(
                                    request.getBankCode()
                            )
                            .build();

            transaction = withdrawRepository.save(transaction);


            TransactionCompletedEvent event =
                    WithdrawCompletedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .transactionId(transaction.getId())
                            .transactionType(TransactionType.WITHDRAWAL)
                            .amount(amount)
                            .occurredAt(OffsetDateTime.now())
                            .walletId(wallet.getId())
                            .build();


            ledgerService.create(
                    wallet,
                    LedgerType.WITHDRAW,
                    amount,
                    balanceBefore,
                    balanceAfter,
                    transaction.getId()
            );


            outboxService.saveTransferCompletedEvent(transaction,event);

            idempotencyService.markCompleted(idempotencyKey
                    ,user.getId()
                    ,TransactionType.WITHDRAWAL
                    ,transaction.getId().toString());

            /*
             * 10. Response
             */
            return WithdrawResponse.builder()
                    .amount(transaction.getAmount())
                    .status(transaction.getStatus())
                    .balance(balanceAfter)
                    .createdAt(transaction.getCreatedAt())
                            .build();

        } catch (Exception ex) {


            idempotencyService.removeValue(idempotencyKey
                    ,user.getId()
                    ,TransactionType.WITHDRAWAL);

            throw ex;
        }
    }
}
