package com.example.digital_wallet.transaction.entity;

import com.example.digital_wallet.wallet.entity.Wallet;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "top_up_transactions",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"wallet_id","idempotency_key"})
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpTransaction extends BaseTransaction {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

}