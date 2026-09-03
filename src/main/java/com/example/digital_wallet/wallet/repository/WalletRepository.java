package com.example.digital_wallet.wallet.repository;

import com.example.digital_wallet.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository
        extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserId(UUID userId);
}