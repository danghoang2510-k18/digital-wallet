package com.example.digital_wallet.transaction.repository;

import com.example.digital_wallet.transaction.entity.TopUpTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TopUpRepository extends JpaRepository<TopUpTransaction, UUID> {
}
