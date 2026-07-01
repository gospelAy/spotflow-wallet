package com.gospelanyanwu.spotflowwallet.service;

import com.gospelanyanwu.spotflowwallet.model.Wallet;
import com.gospelanyanwu.spotflowwallet.model.WalletTransaction;
import com.gospelanyanwu.spotflowwallet.repository.WalletRepository;
import com.gospelanyanwu.spotflowwallet.repository.WalletTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WebhookService(WalletRepository walletRepository,
                          WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public void handleAccountCredited(String webhookEventId, String reference, BigDecimal amount) {
        if (transactionRepository.existsByWebhookEventId(webhookEventId)) {
            log.info("Webhook event {} already processed, skipping", webhookEventId);
            return;
        }

        try {
            creditWallet(webhookEventId, reference, amount);
        } catch (DataIntegrityViolationException e) {
            log.info("Webhook event {} already processed by a concurrent request, skipping", webhookEventId);
        }
    }

    @Transactional
    public void creditWallet(String webhookEventId, String reference, BigDecimal amount) {
        WalletTransaction transaction = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No transaction found for reference: " + reference));

        if (!transaction.isPending()) {
            log.info("Transaction {} is no longer PENDING, skipping", reference);
            return;
        }

        Wallet wallet = walletRepository.findById(transaction.getWalletId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Wallet not found for transaction: " + reference));

        wallet.credit(amount);
        walletRepository.save(wallet);

        transaction.markSuccessful(webhookEventId);
        transactionRepository.save(transaction);
    }
}