package com.example.digital_wallet.transaction.repository;

import com.example.digital_wallet.transaction.entity.WithdrawTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WithdrawRepository
        extends JpaRepository<WithdrawTransaction, UUID> {

    Optional<WithdrawTransaction> findByIdempotencyKey(String idempotencyKey);
}