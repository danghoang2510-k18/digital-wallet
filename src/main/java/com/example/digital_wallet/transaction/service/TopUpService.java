package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.transaction.dto.request.TopUpRequest;
import com.example.digital_wallet.transaction.dto.request.TransferRequest;
import com.example.digital_wallet.transaction.dto.response.TopUpResponse;
import com.example.digital_wallet.transaction.dto.response.TransactionHistoryResponse;
import com.example.digital_wallet.transaction.dto.response.TransferResponse;
import com.example.digital_wallet.transaction.entity.*;
import com.example.digital_wallet.transaction.mapper.LedgerMapper;
import com.example.digital_wallet.transaction.repository.LedgerEntryRepository;
import com.example.digital_wallet.transaction.repository.TopUpRepository;
import com.example.digital_wallet.transaction.repository.TransferRepository;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.user.repository.UserRepository;
import com.example.digital_wallet.wallet.entity.Wallet;
import com.example.digital_wallet.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TopUpService {

    UserRepository userRepository;
    WalletRepository walletRepository;
    LedgerEntryRepository ledgerRepository;
    TopUpRepository topUpRepository;
    TransferRepository transferRepository;
    LedgerMapper ledgerMapper;

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


        TransferTransaction transferTransaction = TransferTransaction.builder()
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(amount)
                .status(TransferStatus.SUCCESS)
                .description(request.getDescription())
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

        return TransferResponse.builder()
                .receiverUsername(receiver.getUsername())
                .amount(amount)
                .senderBalance(senderNewBalance)
                .status("SUCCESS")
                .createdAt(OffsetDateTime.now())
                .build();
    }



    public Page<TransactionHistoryResponse> accessTransactionHistory(int page,
                                                                     int size,
                                                                     String orderBy,
                                                                     LedgerType type,
                                                                     OffsetDateTime from,
                                                                     OffsetDateTime to

    )
    {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_EXISTED));

        Wallet wallet = walletRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.WALLET_NOT_EXISTED));

        Pageable pageable = PageRequest.of(
                page,size,
                Sort.by(
                        Sort.Order.desc(orderBy)
                )
        );



        Specification<LedgerEntry> spec =
                Specification
                        .where(LedgerEntryRepository.hasWalletId(wallet.getId()))
                        .and(LedgerEntryRepository.hasType(type))
                        .and(LedgerEntryRepository.createdAtGreaterThanEqual(from))
                        .and(LedgerEntryRepository.createdAtLessThanEqual(to));

        Page<LedgerEntry> listTransaction =
                ledgerRepository.findAll(spec, pageable);

        return listTransaction.map(
                ledgerMapper::toTransactionHistoryResponse);
    }





}
