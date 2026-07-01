package com.gospelanyanwu.spotflowwallet.dto.request;

import java.math.BigDecimal;

public record SpotflowTransferRequest(
        String reference,
        BigDecimal amount,
        String currency,
        String type,
        Source source,
        Destination destination,
        String narrations
) {
    public record Source(String accountNumber) {
    }

    public record Destination(String accountNumber, String accountName, String bankCode, String branchCode) {
    }
}
