package com.example.digital_wallet.transaction.repository;


import com.example.digital_wallet.transaction.entity.TransferTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<TransferTransaction, UUID> {

    Optional<TransferTransaction> findByIdempotencyKey(String idempotencyKey);
}
