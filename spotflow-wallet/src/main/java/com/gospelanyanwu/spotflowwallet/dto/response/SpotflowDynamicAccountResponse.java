package com.gospelanyanwu.spotflowwallet.dto.response;

public record SpotflowDynamicAccountResponse(
        String accountNumber,
        String bankName,
        String accountName
) {
}
