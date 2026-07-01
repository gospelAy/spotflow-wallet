package com.gospelanyanwu.spotflowwallet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.util.HexFormat;

@Component
public class WebhookSignatureVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${spotflow.webhook-secret}")
    private String webhookSecret;

    public boolean isValid(String rawBody, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        String expectedSignature = computeSignature(rawBody);
        return expectedSignature.equalsIgnoreCase(signatureHeader);
    }

    private String computeSignature(String rawBody) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to compute webhook signature", e);
        }
    }
}
