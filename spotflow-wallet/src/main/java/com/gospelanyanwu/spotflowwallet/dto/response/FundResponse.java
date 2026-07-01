package com.gospelanyanwu.spotflowwallet.dto.response;

import java.math.BigDecimal;

public record FundResponse(
        String reference,
        String dynamicAccountNumber,
        String bankName,
        BigDecimal amount,
        String currency
) {
}
