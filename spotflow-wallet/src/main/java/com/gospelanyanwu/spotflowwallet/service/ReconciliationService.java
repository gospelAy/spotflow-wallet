package com.gospelanyanwu.spotflowwallet.service;

import com.gospelanyanwu.spotflowwallet.config.SpotflowApiClient;
import com.gospelanyanwu.spotflowwallet.dto.response.SpotflowTransferResponse;
import com.gospelanyanwu.spotflowwallet.exception.WalletNotFoundException;
import com.gospelanyanwu.spotflowwallet.model.TransactionStatus;
import com.gospelanyanwu.spotflowwallet.model.TransactionType;
import com.gospelanyanwu.spotflowwallet.model.Wallet;
import com.gospelanyanwu.spotflowwallet.model.WalletTransaction;
import com.gospelanyanwu.spotflowwallet.repository.WalletRepository;
import com.gospelanyanwu.spotflowwallet.repository.WalletTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final WalletTransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final SpotflowApiClient spotflowApiClient;

    @Value("${reconciliation.stuck-after-minutes}")
    private long stuckAfterMinutes;

    public ReconciliationService(WalletTransactionRepository transactionRepository,
                                  WalletRepository walletRepository,
                                  SpotflowApiClient spotflowApiClient) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.spotflowApiClient = spotflowApiClient;
    }

    @Scheduled(cron = "${reconciliation.cron}")
    public void reconcileStuckTransactions() {
        Instant cutoff = Instant.now().minusSeconds(stuckAfterMinutes * 60);
        List<WalletTransaction> stuckTransactions =
                transactionRepository.findByStatusAndCreatedAtBefore(TransactionStatus.PENDING, cutoff);

        for (WalletTransaction transaction : stuckTransactions) {
            try {
                reconcile(transaction);
            } catch (Exception e) {
                log.warn("Failed to reconcile transaction {}: {}", transaction.getReference(), e.getMessage());
            }
        }
    }

    private void reconcile(WalletTransaction transaction) {
        if (transaction.getType() == TransactionType.WITHDRAWAL) {
            reconcileWithdrawal(transaction);
        } else {
            reconcileFund(transaction);
        }
    }

    @Transactional
    public void reconcileWithdrawal(WalletTransaction transaction) {
        SpotflowTransferResponse response = spotflowApiClient.getTransferByReference(transaction.getReference());
        TransactionStatus resolvedStatus = mapSpotflowStatus(response.status());

        if (resolvedStatus == TransactionStatus.SUCCESSFUL) {
            transaction.markSuccessful();
            transactionRepository.save(transaction);
        } else if (resolvedStatus == TransactionStatus.FAILED || resolvedStatus == TransactionStatus.ABANDONED) {
            reverseWithdrawalDebit(transaction);
            if (resolvedStatus == TransactionStatus.FAILED) {
                transaction.markFailed("Spotflow reported transfer as failed");
            } else {
                transaction.markAbandoned();
            }
            transactionRepository.save(transaction);
        }
    }

    @Transactional
    public void reconcileFund(WalletTransaction transaction) {
        Instant expiresAt = transaction.getDynamicAccountExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            transaction.markAbandoned();
            transactionRepository.save(transaction);
        }
    }

    private void reverseWithdrawalDebit(WalletTransaction transaction) {
        Wallet wallet = walletRepository.findById(transaction.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for transaction " + transaction.getReference()));
        wallet.credit(transaction.getAmount());
        walletRepository.save(wallet);
    }

    private TransactionStatus mapSpotflowStatus(String status) {
        if (status == null) {
            return TransactionStatus.PENDING;
        }
        return switch (status.toLowerCase()) {
            case "successful", "success" -> TransactionStatus.SUCCESSFUL;
            case "failed" -> TransactionStatus.FAILED;
            case "abandoned" -> TransactionStatus.ABANDONED;
            default -> TransactionStatus.PENDING;
        };
    }
}
