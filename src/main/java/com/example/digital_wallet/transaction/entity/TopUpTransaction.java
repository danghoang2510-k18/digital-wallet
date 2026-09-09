package com.example.digital_wallet.transaction.entity;

import com.example.digital_wallet.wallet.entity.Wallet;
import jakarta.persistence.*;
import lombok.*;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopUpStatus status;

    @Column(unique = true,name = "idempotency_key")
    private String idempotencyKey;

    private String paymentReference;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    private OffsetDateTime completedAt;
}