package com.gospelanyanwu.spotflowwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gospelanyanwu.spotflowwallet.config.WebhookSignatureVerifier;
import com.gospelanyanwu.spotflowwallet.dto.WebhookPayload;
import com.gospelanyanwu.spotflowwallet.service.WebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private static final String ACCOUNT_CREDIT_SUCCESSFUL = "account_credit_successful";

    private final WebhookSignatureVerifier signatureVerifier;
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    public WebhookController(WebhookSignatureVerifier signatureVerifier,
                              WebhookService webhookService,
                              ObjectMapper objectMapper) {
        this.signatureVerifier = signatureVerifier;
        this.webhookService = webhookService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/spotflow")
    public ResponseEntity<Void> handleSpotflowWebhook(
            @RequestBody String rawBody,
            @RequestHeader("x-spotflow-signature") String signature,
            @RequestHeader("webhook-id") String webhookId) throws Exception {

        // TODO:
        // if (!signatureVerifier.isValid(rawBody, signature)) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        // }

        WebhookPayload payload = objectMapper.readValue(rawBody, WebhookPayload.class);

        if (ACCOUNT_CREDIT_SUCCESSFUL.equals(payload.event())) {
            webhookService.handleAccountCredited(webhookId, payload.data().reference(), payload.data().amount());
        }

        return ResponseEntity.ok().build();
    }
}
