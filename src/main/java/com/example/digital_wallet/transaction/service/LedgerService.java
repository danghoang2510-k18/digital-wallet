package com.example.digital_wallet.transaction.service;


import com.example.digital_wallet.transaction.entity.LedgerEntry;
import com.example.digital_wallet.transaction.entity.LedgerType;
import com.example.digital_wallet.transaction.repository.LedgerEntryRepository;
import com.example.digital_wallet.wallet.entity.Wallet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class LedgerService {

    LedgerEntryRepository ledgerRepository;
    public LedgerEntry create(
            Wallet wallet,
            LedgerType type,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            UUID referenceId
    ) {

        LedgerEntry ledger = LedgerEntry.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceId(referenceId)
                .build();

        return ledgerRepository.save(ledger);
    }
}
