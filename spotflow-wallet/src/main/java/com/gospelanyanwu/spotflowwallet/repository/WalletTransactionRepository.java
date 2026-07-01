package com.gospelanyanwu.spotflowwallet.repository;

import com.gospelanyanwu.spotflowwallet.model.TransactionStatus;
import com.gospelanyanwu.spotflowwallet.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    Optional<WalletTransaction> findByReference(String reference);

    boolean existsByWebhookEventId(String webhookEventId);

    List<WalletTransaction> findByStatusAndCreatedAtBefore(TransactionStatus status, Instant cutoff);
}
