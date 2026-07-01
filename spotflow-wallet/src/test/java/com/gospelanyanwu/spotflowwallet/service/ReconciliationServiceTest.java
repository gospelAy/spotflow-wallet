package com.gospelanyanwu.spotflowwallet.service;

import com.gospelanyanwu.spotflowwallet.config.SpotflowApiClient;
import com.gospelanyanwu.spotflowwallet.model.TransactionStatus;
import com.gospelanyanwu.spotflowwallet.model.TransactionType;
import com.gospelanyanwu.spotflowwallet.model.WalletTransaction;
import com.gospelanyanwu.spotflowwallet.repository.WalletRepository;
import com.gospelanyanwu.spotflowwallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReconciliationServiceTest {

    private final WalletTransactionRepository transactionRepository = mock(WalletTransactionRepository.class);
    private final WalletRepository walletRepository = mock(WalletRepository.class);
    private final SpotflowApiClient spotflowApiClient = mock(SpotflowApiClient.class);
    private final ReconciliationService reconciliationService =
            new ReconciliationService(transactionRepository, walletRepository, spotflowApiClient);

    @Test
    void expiredFundTransactionIsAbandonedWhileUnexpiredOneStaysPending() {
        WalletTransaction expired = fundTransaction(Instant.now().minusSeconds(60));
        WalletTransaction notExpired = fundTransaction(Instant.now().plusSeconds(600));

        when(transactionRepository.findByStatusAndCreatedAtBefore(eq(TransactionStatus.PENDING), any()))
                .thenReturn(List.of(expired, notExpired));

        reconciliationService.reconcileStuckTransactions();

        assertEquals(TransactionStatus.ABANDONED, expired.getStatus());
        assertEquals(TransactionStatus.PENDING, notExpired.getStatus());
        verifyNoInteractions(spotflowApiClient);
    }

    private WalletTransaction fundTransaction(Instant dynamicAccountExpiresAt) {
        return WalletTransaction.builder()
                .id(UUID.randomUUID())
                .walletId(UUID.randomUUID())
                .type(TransactionType.FUND)
                .status(TransactionStatus.PENDING)
                .amount(BigDecimal.valueOf(1000))
                .currency("NGN")
                .reference("FUND-" + UUID.randomUUID())
                .dynamicAccountExpiresAt(dynamicAccountExpiresAt)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
