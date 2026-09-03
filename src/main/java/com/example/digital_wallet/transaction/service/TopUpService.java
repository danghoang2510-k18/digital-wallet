package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.transaction.dto.request.TopUpRequest;
import com.example.digital_wallet.transaction.dto.request.TransferRequest;
import com.example.digital_wallet.transaction.dto.response.TopUpResponse;
import com.example.digital_wallet.transaction.dto.response.TransferResponse;
import com.example.digital_wallet.transaction.entity.LedgerEntry;
import com.example.digital_wallet.transaction.entity.LedgerType;
import com.example.digital_wallet.transaction.entity.TopUpStatus;
import com.example.digital_wallet.transaction.entity.TopUpTransaction;
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
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TopUpService {

    UserRepository userRepository;
    WalletRepository walletRepository;
    LedgerEntryRepository ledgerRepository;
    TopUpRepository topUpRepository;

    @Transactional
    public TopUpResponse topUp(TopUpRequest request) throws InterruptedException {

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

        Thread.sleep(5000);

        BigDecimal newBalance = oldBalance.add(amount);

        TopUpTransaction transaction = TopUpTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .status(TopUpStatus.SUCCESS)
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

        return TopUpResponse.builder()
                .amount(amount)
                .balance(newBalance)
                .build();
    }


    @Transactional
    public TransferResponse transfer(TransferRequest request) throws InterruptedException {

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


        Thread.sleep(5000);
        walletRepository.save(senderWallet);

        walletRepository.save(receiverWallet);

        LedgerEntry senderLedger = LedgerEntry.builder()
                .wallet(senderWallet)
                .type(LedgerType.TRANSFER_OUT)
                .amount(amount.negate())
                .balanceBefore(senderOldBalance)
                .balanceAfter(senderNewBalance)
                .build();

        LedgerEntry receiverLedger = LedgerEntry.builder()
                .wallet(receiverWallet)
                .type(LedgerType.TRANSFER_IN)
                .amount(amount)
                .balanceBefore(receiverOldBalance)
                .balanceAfter(receiverNewBalance)
                .build();

        ledgerRepository.save(senderLedger);
        ledgerRepository.save(receiverLedger);

        return TransferResponse.builder()
                .receiverUsername(receiver.getUsername())
                .amount(amount)
                .senderBalance(senderNewBalance)
                .status("SUCCESS")
                .createdAt(OffsetDateTime.now())
                .build();
    }


}
