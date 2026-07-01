package com.gospelanyanwu.spotflowwallet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_transaction")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {
    @Id
    private UUID id;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(name = "spotflow_reference")
    private String spotflowReference;

    @Column(name = "webhook_event_id", unique = true)
    private String webhookEventId;

    @Column(name = "dynamic_account_number")
    private String dynamicAccountNumber;

    @Column(name = "dynamic_account_bank")
    private String dynamicAccountBank;

    @Column(name = "dynamic_account_expires_at")
    private Instant dynamicAccountExpiresAt;

    @Column(name = "destination_account_number")
    private String destinationAccountNumber;

    @Column(name = "destination_bank_code")
    private String destinationBankCode;

    @Column(name = "destination_account_name")
    private String destinationAccountName;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void attachDynamicAccount(String accountNumber, String bankName, Instant expiresAt) {
        this.dynamicAccountNumber = accountNumber;
        this.dynamicAccountBank = bankName;
        this.dynamicAccountExpiresAt = expiresAt;
        this.updatedAt = Instant.now();
    }

    public void attachSpotflowReference(String spotflowReference) {
        this.spotflowReference = spotflowReference;
        this.updatedAt = Instant.now();
    }

    public void markSuccessful(String webhookEventId) {
        this.status = TransactionStatus.SUCCESSFUL;
        this.webhookEventId = webhookEventId;
        this.updatedAt = Instant.now();
    }

    public void markSuccessful() {
        this.status = TransactionStatus.SUCCESSFUL;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String failureReason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = failureReason;
        this.updatedAt = Instant.now();
    }

    public void markAbandoned() {
        this.status = TransactionStatus.ABANDONED;
        this.updatedAt = Instant.now();
    }

    public boolean isPending() {
        return this.status == TransactionStatus.PENDING;
    }
}