package com.gospelanyanwu.spotflowwallet.dto;

import java.math.BigDecimal;

public record WebhookPayload(
        String event,
        Data data
) {
    public record Data(
            String reference,
            BigDecimal amount,
            String status
    ) {
    }
}
