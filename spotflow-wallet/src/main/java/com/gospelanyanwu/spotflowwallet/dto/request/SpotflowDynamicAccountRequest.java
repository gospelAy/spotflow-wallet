package com.gospelanyanwu.spotflowwallet.dto.request;

import java.math.BigDecimal;

public record SpotflowDynamicAccountRequest(
        String currency,
        String accountName,
        BigDecimal amount,
        long expiresIn
) {
}
