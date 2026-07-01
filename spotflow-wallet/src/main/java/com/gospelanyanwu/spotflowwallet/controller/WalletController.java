package com.gospelanyanwu.spotflowwallet.controller;

import com.gospelanyanwu.spotflowwallet.dto.request.FundRequest;
import com.gospelanyanwu.spotflowwallet.dto.response.FundResponse;
import com.gospelanyanwu.spotflowwallet.dto.request.WithdrawRequest;
import com.gospelanyanwu.spotflowwallet.dto.response.WithdrawResponse;
import com.gospelanyanwu.spotflowwallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/fund")
    public ResponseEntity<FundResponse> fund(@Valid @RequestBody FundRequest request) {
        FundResponse response = walletService.fund(request.userId(), request.amount(), request.currency());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        WithdrawResponse response = walletService.withdraw(
                request.userId(),
                request.amount(),
                request.destinationAccountNumber(),
                request.destinationBankCode(),
                request.destinationAccountName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
