package com.example.digital_wallet.transaction.repository;

import com.example.digital_wallet.transaction.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

}
