package com.gospelanyanwu.spotflowwallet.config;

import com.gospelanyanwu.spotflowwallet.dto.request.SpotflowDynamicAccountRequest;
import com.gospelanyanwu.spotflowwallet.dto.response.SpotflowDynamicAccountResponse;
import com.gospelanyanwu.spotflowwallet.dto.request.SpotflowTransferRequest;
import com.gospelanyanwu.spotflowwallet.dto.response.SpotflowTransferResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "spotflow", url = "${spotflow.base-url}", configuration = SpotflowFeignConfig.class)
public interface SpotflowApiClient {

    @PostMapping("/virtual-accounts/temporary")
    SpotflowDynamicAccountResponse createDynamicAccount(@RequestBody SpotflowDynamicAccountRequest request);

    @PostMapping("/transfers")
    SpotflowTransferResponse createTransfer(@RequestBody SpotflowTransferRequest request);

    @GetMapping("/transfers/reference/{reference}")
    SpotflowTransferResponse getTransferByReference(@PathVariable("reference") String reference);
}
