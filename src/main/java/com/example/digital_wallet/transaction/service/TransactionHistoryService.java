package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.common.security.CurrentUserService;
import com.example.digital_wallet.transaction.dto.response.TransactionHistoryResponse;
import com.example.digital_wallet.transaction.entity.LedgerEntry;
import com.example.digital_wallet.transaction.entity.LedgerType;
import com.example.digital_wallet.transaction.entity.TransactionSortField;
import com.example.digital_wallet.transaction.mapper.LedgerMapper;
import com.example.digital_wallet.transaction.repository.LedgerEntryRepository;
import com.example.digital_wallet.user.entity.User;
import com.example.digital_wallet.wallet.entity.Wallet;
import com.example.digital_wallet.wallet.repository.WalletRepository;
import com.example.digital_wallet.wallet.service.WalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static com.example.digital_wallet.transaction.entity.TransactionSortField.CREATED_AT;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TransactionHistoryService {

    WalletRepository walletRepository;
    LedgerEntryRepository ledgerRepository;
    LedgerMapper ledgerMapper;
    CurrentUserService currentUserService;
    WalletService walletService;


    public Page<TransactionHistoryResponse> accessTransactionHistory(int page,
                                                                     int size,
                                                                     TransactionSortField sort,
                                                                     LedgerType type,
                                                                     OffsetDateTime from,
                                                                     OffsetDateTime to

    )
    {

        User user = currentUserService.getCurrentUser();

        Wallet wallet = walletService.getWalletByUserId(user.getId());

        Sort.Order order =
                switch (sort) {
                    case CREATED_AT ->
                            Sort.Order.desc("createdAt");
                };

        Pageable pageable = PageRequest.of(
                page,size,
//                Sort.by(
//                        Sort.Order.desc(orderBy)
//                )
                Sort.by(order)
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
