package com.gospelanyanwu.spotflowwallet.dto.response;

import java.math.BigDecimal;

public record WithdrawResponse(
        String reference,
        BigDecimal amount,
        String currency
) {
}
