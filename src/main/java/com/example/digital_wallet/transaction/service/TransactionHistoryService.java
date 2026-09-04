package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.exception.AppException;
import com.example.digital_wallet.common.exception.ErrorCode;
import com.example.digital_wallet.transaction.dto.response.TransactionHistoryResponse;
import com.example.digital_wallet.transaction.entity.LedgerEntry;
import com.example.digital_wallet.transaction.entity.LedgerType;
import com.example.digital_wallet.transaction.mapper.LedgerMapper;
import com.example.digital_wallet.transaction.repository.LedgerEntryRepository;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.user.repository.UserRepository;
import com.example.digital_wallet.wallet.entity.Wallet;
import com.example.digital_wallet.wallet.repository.WalletRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TransactionHistoryService {

    UserRepository userRepository;
    WalletRepository walletRepository;
    LedgerEntryRepository ledgerRepository;
    LedgerMapper ledgerMapper;


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
