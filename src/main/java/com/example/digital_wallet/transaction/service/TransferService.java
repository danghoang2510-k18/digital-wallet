package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.redis.service.IdempotencyService;
import com.example.digital_wallet.transaction.dto.request.TransferRequest;
import com.example.digital_wallet.transaction.dto.response.TransferResponse;
import com.example.digital_wallet.transaction.entity.LedgerEntry;
import com.example.digital_wallet.transaction.entity.LedgerType;
import com.example.digital_wallet.transaction.entity.TransferStatus;
import com.example.digital_wallet.transaction.entity.TransferTransaction;
import com.example.digital_wallet.transaction.mapper.TransferMapper;
import com.example.digital_wallet.transaction.repository.LedgerEntryRepository;
import com.example.digital_wallet.transaction.repository.TransferRepository;
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
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TransferService {

    IdempotencyService idempotencyService;
    TransferRepository transferRepository;
    TransferMapper transferMapper;
    UserRepository userRepository;
    WalletRepository walletRepository;
    LedgerEntryRepository ledgerRepository;


    @Transactional
    public TransferResponse transfer(TransferRequest request, String idempotencyKey

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
                Optional<TransferTransaction> existingTransaction =
                        transferRepository
                                .findByIdempotencyKey(idempotencyKey);

                if (existingTransaction.isPresent()) {
                    return transferMapper.toTransferResponse(existingTransaction.get());
                }
            }

        }


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String senderUsername = authentication.getName();

        User sender = userRepository
                .findByUsername(senderUsername)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_EXISTED));

        User receiver = userRepository
                .findByUsername(request.getReceiverUsername())
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_EXISTED));

        if (sender.getId().equals(receiver.getId())) {
            throw new AppException(ErrorCode.CANNOT_TRANSFER_TO_SELF);
        }

        Wallet senderWallet = walletRepository
                .findByUserId(sender.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.WALLET_NOT_EXISTED));

        Wallet receiverWallet = walletRepository
                .findByUserId(receiver.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.WALLET_NOT_EXISTED));

        BigDecimal amount = request.getAmount();

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        BigDecimal senderOldBalance =
                senderWallet.getBalance();

        BigDecimal receiverOldBalance =
                receiverWallet.getBalance();

        BigDecimal senderNewBalance =
                senderOldBalance.subtract(amount);

        BigDecimal receiverNewBalance =
                receiverOldBalance.add(amount);

        senderWallet.setBalance(senderNewBalance);

        receiverWallet.setBalance(receiverNewBalance);


        walletRepository.save(senderWallet);

        walletRepository.save(receiverWallet);


        TransferTransaction transferTransaction = TransferTransaction.builder()
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(amount)
                .status(TransferStatus.SUCCESS)
                .description(request.getDescription())
                .idempotencyKey(idempotencyKey)
                .build();

        transferRepository.save(transferTransaction);


        LedgerEntry senderLedger = LedgerEntry.builder()
                .wallet(senderWallet)
                .type(LedgerType.TRANSFER_OUT)
                .amount(amount.negate())
                .balanceBefore(senderOldBalance)
                .balanceAfter(senderNewBalance)
                .referenceId(transferTransaction.getId())
                .build();

        LedgerEntry receiverLedger = LedgerEntry.builder()
                .wallet(receiverWallet)
                .type(LedgerType.TRANSFER_IN)
                .amount(amount)
                .balanceBefore(receiverOldBalance)
                .balanceAfter(receiverNewBalance)
                .referenceId(transferTransaction.getId())
                .build();



        ledgerRepository.save(senderLedger);
        ledgerRepository.save(receiverLedger);

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
