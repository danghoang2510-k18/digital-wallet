package com.example.digital_wallet.transaction.entity;

import com.example.digital_wallet.wallet.entity.Wallet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "withdraw_transactions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"wallet_id","idempotency_key"})
        })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawTransaction extends BaseTransaction {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private String bankAccountNumber;

    @Column(nullable = false)
    private String bankCode;
}