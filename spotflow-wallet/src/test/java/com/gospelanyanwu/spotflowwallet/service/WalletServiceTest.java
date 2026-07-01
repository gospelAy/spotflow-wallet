package com.gospelanyanwu.spotflowwallet.service;

import com.gospelanyanwu.spotflowwallet.config.SpotflowApiClient;
import com.gospelanyanwu.spotflowwallet.exception.InsufficientBalanceException;
import com.gospelanyanwu.spotflowwallet.model.Wallet;
import com.gospelanyanwu.spotflowwallet.repository.WalletRepository;
import com.gospelanyanwu.spotflowwallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WalletServiceTest {

    private final WalletRepository walletRepository = mock(WalletRepository.class);
    private final WalletTransactionRepository transactionRepository = mock(WalletTransactionRepository.class);
    private final SpotflowApiClient spotflowApiClient = mock(SpotflowApiClient.class);
    private final WalletService walletService =
            new WalletService(walletRepository, transactionRepository, spotflowApiClient);

    @Test
    void withdrawalLargerThanBalanceThrowsAndNeverCallsSpotflow() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .balance(BigDecimal.valueOf(1000))
                .currency("NGN")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(walletRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class, () ->
                walletService.withdraw(userId, BigDecimal.valueOf(5000), "0123456789", "044", "John Doe"));

        verifyNoInteractions(spotflowApiClient);
    }
}
