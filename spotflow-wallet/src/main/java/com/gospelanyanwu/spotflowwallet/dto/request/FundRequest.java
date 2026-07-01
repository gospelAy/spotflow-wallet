package com.gospelanyanwu.spotflowwallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record FundRequest(
        @NotNull UUID userId,
        @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String currency
) {
}
