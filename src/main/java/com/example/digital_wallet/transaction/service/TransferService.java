package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.common.security.CurrentUserService;
import com.example.digital_wallet.kafka.event.OutboxService;
import com.example.digital_wallet.kafka.event.TransactionCompletedEvent;
import com.example.digital_wallet.kafka.event.TransferCompletedEvent;
import com.example.digital_wallet.redis.service.IdempotencyService;
import com.example.digital_wallet.transaction.dto.request.TransferRequest;
import com.example.digital_wallet.transaction.dto.response.TransferResponse;
import com.example.digital_wallet.transaction.entity.LedgerType;
import com.example.digital_wallet.transaction.entity.TransactionStatus;
import com.example.digital_wallet.transaction.entity.TransactionType;
import com.example.digital_wallet.transaction.entity.TransferTransaction;
import com.example.digital_wallet.transaction.mapper.TransferMapper;
import com.example.digital_wallet.transaction.repository.TransferRepository;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.user.service.UserService;
import com.example.digital_wallet.wallet.entity.Wallet;
import com.example.digital_wallet.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TransferService {

    IdempotencyService idempotencyService;
    TransferRepository transferRepository;
    TransferMapper transferMapper;
    UserService userService;
    CurrentUserService currentUserService;
    WalletService walletService;
    LedgerService ledgerService;
    OutboxService outboxService;


    @Transactional
    public TransferResponse transfer(TransferRequest request, String idempotencyKey

    ) {

        User sender = currentUserService.getCurrentUser();

        boolean acquired = idempotencyService.tryAcquire(idempotencyKey, sender.getId(), TransactionType.TRANSFER);
        if (!acquired) {

            String value = idempotencyService.getValue(idempotencyKey, sender.getId(), TransactionType.TRANSFER);

            if ("PROCESSING".equals(value)) {
                throw new AppException(ErrorCode.TRANSACTION_PROCESSING);
            } else {
                Optional<TransferTransaction> existingTransaction =
                        transferRepository
                                .findByIdempotencyKey(idempotencyKey);

                if (existingTransaction.isPresent()) {
                    return transferMapper.toTransferResponse(existingTransaction.get());
                }
            }

        }


        try {
            User receiver = userService.getUserByUserName(request.getReceiverUsername());

            if (sender.getId().equals(receiver.getId())) {
                throw new AppException(ErrorCode.CANNOT_TRANSFER_TO_SELF);
            }

            Wallet senderWallet = walletService.getWalletByUserId(sender.getId());

            Wallet receiverWallet = walletService.getWalletByUserId(receiver.getId());


            BigDecimal amount = request.getAmount();


            BigDecimal senderOldBalance =
                    senderWallet.getBalance();

            BigDecimal receiverOldBalance =
                    receiverWallet.getBalance();


            BigDecimal senderNewBalance = walletService.debit(senderWallet, amount);


            BigDecimal receiverNewBalance = walletService.credit(receiverWallet, amount);


            TransferTransaction transaction = TransferTransaction.builder()
                    .senderWallet(senderWallet)
                    .receiverWallet(receiverWallet)
                    .amount(amount)
                    .status(TransactionStatus.SUCCESS)
                    .description(request.getDescription())
                    .idempotencyKey(idempotencyKey)
                    .build();

            transaction=transferRepository.save(transaction);

            TransactionCompletedEvent event =
                    TransferCompletedEvent.builder()
                            .eventId(UUID.randomUUID())
                            .transactionId(transaction.getId())
                            .transactionType(TransactionType.TRANSFER)
                            .amount(amount)
                            .occurredAt(OffsetDateTime.now())
                            .senderWalletId(senderWallet.getId())
                            .receiverWalletId(receiverWallet.getId())
                            .build();

//        eventProducer.publishTransferCompleted(event);


            ledgerService.create(
                    senderWallet,
                    LedgerType.TRANSFER_OUT,
                    amount.negate(),
                    senderOldBalance,
                    senderNewBalance,
                    transaction.getId()
            );

            ledgerService.create(
                    receiverWallet,
                    LedgerType.TRANSFER_IN,
                    amount,
                    receiverOldBalance,
                    receiverNewBalance,
                    transaction.getId()
            );


            outboxService.saveTransferCompletedEvent(
                    transaction,
                    event
            );


            idempotencyService.markCompleted(
                    idempotencyKey,
                    sender.getId(),
                    TransactionType.TRANSFER,
                    transaction.getId().toString()
            );

            return TransferResponse.builder()
                    .transactionId(transaction.getId())
                    .receiverUsername(receiver.getUsername())
                    .amount(amount)
                    .senderBalance(senderNewBalance)
                    .status(transaction.getStatus())
                    .createdAt(transaction.getCreatedAt())
                    .build();
        }
        catch (Exception ex)
        {
            idempotencyService.removeValue(idempotencyKey
                    ,sender.getId()
                    ,TransactionType.WITHDRAWAL);

            throw ex;
        }
    }

}
