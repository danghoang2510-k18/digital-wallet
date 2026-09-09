package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.common.security.CurrentUserService;
import com.example.digital_wallet.kafka.event.OutboxService;
import com.example.digital_wallet.kafka.event.TransferCompletedEvent;
import com.example.digital_wallet.kafka.producer.TransactionEventProducer;
import com.example.digital_wallet.redis.service.IdempotencyService;
import com.example.digital_wallet.transaction.dto.request.TransferRequest;
import com.example.digital_wallet.transaction.dto.response.TransferResponse;
import com.example.digital_wallet.transaction.entity.LedgerType;
import com.example.digital_wallet.transaction.entity.TransferStatus;
import com.example.digital_wallet.transaction.entity.TransferTransaction;
import com.example.digital_wallet.transaction.mapper.TransferMapper;
import com.example.digital_wallet.transaction.repository.TransferRepository;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.user.repository.UserRepository;
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
                Optional<TransferTransaction> existingTransaction =
                        transferRepository
                                .findByIdempotencyKey(idempotencyKey);

                if (existingTransaction.isPresent()) {
                    return transferMapper.toTransferResponse(existingTransaction.get());
                }
            }

        }


        User sender = currentUserService.getCurrentUser();

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


        BigDecimal senderNewBalance = walletService.debit(senderWallet,amount);


        BigDecimal receiverNewBalance = walletService.credit(receiverWallet,amount);




        TransferTransaction transferTransaction = TransferTransaction.builder()
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(amount)
                .status(TransferStatus.SUCCESS)
                .description(request.getDescription())
                .idempotencyKey(idempotencyKey)
                .build();

        transferRepository.save(transferTransaction);

        TransferCompletedEvent event =
                TransferCompletedEvent.builder()
                        .eventId(UUID.randomUUID())
                        .transactionId(transferTransaction.getId())
                        .senderWalletId(senderWallet.getId())
                        .receiverWalletId(receiverWallet.getId())
                        .amount(amount)
                        .occurredAt(OffsetDateTime.now())
                                .build();

//        eventProducer.publishTransferCompleted(event);




        ledgerService.create(
                senderWallet,
                LedgerType.TRANSFER_OUT,
                amount.negate(),
                senderOldBalance,
                senderNewBalance,
                transferTransaction.getId()
        );

        ledgerService.create(
                receiverWallet,
                LedgerType.TRANSFER_IN,
                amount,
                receiverOldBalance,
                receiverNewBalance,
                transferTransaction.getId()
        );



        outboxService.saveTransferCompletedEvent(
                transferTransaction,
                event
        );
        Thread.sleep(50000);




        idempotencyService.markCompleted(
                idempotencyKey,
                transferTransaction.getId().toString()
        );

        return TransferResponse.builder()
                .transactionId(transferTransaction.getId())
                .receiverUsername(receiver.getUsername())
                .amount(amount)
                .senderBalance(senderNewBalance)
                .status("SUCCESS")
                .createdAt(OffsetDateTime.now())
                .build();
    }

}
