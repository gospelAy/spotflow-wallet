package com.gospelanyanwu.spotflowwallet.dto.response;

import java.math.BigDecimal;

public record SpotflowTransferResponse(
        String reference,
        String spotflowreference,
        BigDecimal amount,
        String currency,
        String status
) {
}
