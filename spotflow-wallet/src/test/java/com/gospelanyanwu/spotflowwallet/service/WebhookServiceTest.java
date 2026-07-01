package com.gospelanyanwu.spotflowwallet.service;

import com.gospelanyanwu.spotflowwallet.model.TransactionStatus;
import com.gospelanyanwu.spotflowwallet.model.TransactionType;
import com.gospelanyanwu.spotflowwallet.model.Wallet;
import com.gospelanyanwu.spotflowwallet.model.WalletTransaction;
import com.gospelanyanwu.spotflowwallet.repository.WalletRepository;
import com.gospelanyanwu.spotflowwallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebhookServiceTest {

    private final WalletRepository walletRepository = mock(WalletRepository.class);
    private final WalletTransactionRepository transactionRepository = mock(WalletTransactionRepository.class);
    private final WebhookService webhookService = new WebhookService(walletRepository, transactionRepository);

    @Test
    void duplicateWebhookEventOnlyCreditsWalletOnce() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(walletId)
                .userId(UUID.randomUUID())
                .balance(BigDecimal.ZERO)
                .currency("NGN")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        WalletTransaction transaction = WalletTransaction.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(TransactionType.FUND)
                .status(TransactionStatus.PENDING)
                .amount(BigDecimal.valueOf(5000))
                .currency("NGN")
                .reference("FUND-123")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        String webhookEventId = "evt-1";

        when(transactionRepository.existsByWebhookEventId(webhookEventId))
                .thenReturn(false)
                .thenReturn(true);
        when(transactionRepository.findByReference("FUND-123")).thenReturn(Optional.of(transaction));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        webhookService.handleAccountCredited(webhookEventId, "FUND-123", BigDecimal.valueOf(5000));
        webhookService.handleAccountCredited(webhookEventId, "FUND-123", BigDecimal.valueOf(5000));

        assertEquals(0, BigDecimal.valueOf(5000).compareTo(wallet.getBalance()));
        assertEquals(TransactionStatus.SUCCESSFUL, transaction.getStatus());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }
}
