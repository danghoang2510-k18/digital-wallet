package com.example.digital_wallet.transaction.repository;

import com.example.digital_wallet.transaction.entity.LedgerEntry;
import com.example.digital_wallet.transaction.entity.LedgerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID>,
        JpaSpecificationExecutor<LedgerEntry>
{

    Page<LedgerEntry> findByWallet_Id(Specification spec, Pageable pageable);

    public static Specification<LedgerEntry> hasWalletId(UUID walletId) {
        return (root, query, cb) ->
                cb.equal(root.get("wallet").get("id"), walletId);
    }

    public static Specification<LedgerEntry> hasType(LedgerType type) {
        return (root, query, cb) -> {
            if (type == null) {
                return null;
            }

            return cb.equal(root.get("type"), type);
        };


    }

    public static Specification<LedgerEntry> createdAtGreaterThanEqual(
            OffsetDateTime from
    ) {
        return (root, query, cb) -> {
            if (from == null) {
                return null;
            }

            return cb.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    from
            );
        };
    }



    public static Specification<LedgerEntry> createdAtLessThanEqual(
            OffsetDateTime to
    ) {
        return (root, query, cb) -> {
            if (to == null) {
                return null;
            }

            return cb.lessThanOrEqualTo(
                    root.get("createdAt"),
                    to
            );
        };
    }

}
