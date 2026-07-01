package com.gospelanyanwu.spotflowwallet.service;

import com.gospelanyanwu.spotflowwallet.config.SpotflowApiClient;
import com.gospelanyanwu.spotflowwallet.config.SpotflowApiException;
import com.gospelanyanwu.spotflowwallet.dto.response.FundResponse;
import com.gospelanyanwu.spotflowwallet.dto.request.SpotflowDynamicAccountRequest;
import com.gospelanyanwu.spotflowwallet.dto.response.SpotflowDynamicAccountResponse;
import com.gospelanyanwu.spotflowwallet.dto.request.SpotflowTransferRequest;
import com.gospelanyanwu.spotflowwallet.dto.response.SpotflowTransferResponse;
import com.gospelanyanwu.spotflowwallet.dto.response.WithdrawResponse;
import com.gospelanyanwu.spotflowwallet.exception.InsufficientBalanceException;
import com.gospelanyanwu.spotflowwallet.exception.WalletNotFoundException;
import com.gospelanyanwu.spotflowwallet.model.TransactionStatus;
import com.gospelanyanwu.spotflowwallet.model.TransactionType;
import com.gospelanyanwu.spotflowwallet.model.Wallet;
import com.gospelanyanwu.spotflowwallet.model.WalletTransaction;
import com.gospelanyanwu.spotflowwallet.repository.WalletRepository;
import com.gospelanyanwu.spotflowwallet.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class WalletService {

    private static final long DYNAMIC_ACCOUNT_EXPIRES_IN_SECONDS = 1800;

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final SpotflowApiClient spotflowApiClient;

    @Value("${spotflow.main-account-number}")
    private String mainAccountNumber;

    public WalletService(WalletRepository walletRepository,
                          WalletTransactionRepository transactionRepository,
                          SpotflowApiClient spotflowApiClient) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.spotflowApiClient = spotflowApiClient;
    }

    @Transactional
    public FundResponse fund(UUID userId, BigDecimal amount, String currency) {
        Wallet wallet = findOrCreateWallet(userId, currency);
        String reference = "FUND-" + UUID.randomUUID();

        WalletTransaction transaction = WalletTransaction.builder()
                .id(UUID.randomUUID())
                .walletId(wallet.getId())
                .type(TransactionType.FUND)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .currency(currency)
                .reference(reference)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        transactionRepository.save(transaction);

        SpotflowDynamicAccountResponse spotflowResponse;
        try {
            spotflowResponse = spotflowApiClient.createDynamicAccount(new SpotflowDynamicAccountRequest(
                    currency, wallet.getUserId().toString(), amount, DYNAMIC_ACCOUNT_EXPIRES_IN_SECONDS));
        } catch (SpotflowApiException e) {
            transaction.markFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw e;
        }

        Instant expiresAt = Instant.now().plusSeconds(DYNAMIC_ACCOUNT_EXPIRES_IN_SECONDS);
        transaction.attachDynamicAccount(spotflowResponse.accountNumber(), spotflowResponse.bankName(), expiresAt);
        transactionRepository.save(transaction);

        return new FundResponse(reference, spotflowResponse.accountNumber(), spotflowResponse.bankName(), amount, currency);
    }

    @Transactional
    public WithdrawResponse withdraw(UUID userId, BigDecimal amount, String destinationAccountNumber,
                                      String destinationBankCode, String destinationAccountName) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user " + userId));

        if (!wallet.hasSufficientBalance(amount)) {
            throw new InsufficientBalanceException("Wallet balance is insufficient for this withdrawal");
        }

        wallet.debit(amount);
        walletRepository.save(wallet);

        String reference = "WD-" + UUID.randomUUID();
        WalletTransaction transaction = WalletTransaction.builder()
                .id(UUID.randomUUID())
                .walletId(wallet.getId())
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .currency(wallet.getCurrency())
                .reference(reference)
                .destinationAccountNumber(destinationAccountNumber)
                .destinationBankCode(destinationBankCode)
                .destinationAccountName(destinationAccountName)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        transactionRepository.save(transaction);

        try {
            SpotflowTransferResponse spotflowResponse = spotflowApiClient.createTransfer(new SpotflowTransferRequest(
                    reference,
                    amount,
                    wallet.getCurrency(),
                    "withdrawal",
                    new SpotflowTransferRequest.Source(mainAccountNumber),
                    new SpotflowTransferRequest.Destination(
                            destinationAccountNumber, destinationAccountName, destinationBankCode, null),
                    "Wallet withdrawal"
            ));
            transaction.attachSpotflowReference(spotflowResponse.spotflowreference());
            transactionRepository.save(transaction);
        } catch (SpotflowApiException e) {
            wallet.credit(amount);
            walletRepository.save(wallet);
            transaction.markFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw e;
        }

        return new WithdrawResponse(reference, amount, wallet.getCurrency());
    }

    private Wallet findOrCreateWallet(UUID userId, String currency) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .balance(BigDecimal.ZERO)
                        .currency(currency)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()));
    }
}
