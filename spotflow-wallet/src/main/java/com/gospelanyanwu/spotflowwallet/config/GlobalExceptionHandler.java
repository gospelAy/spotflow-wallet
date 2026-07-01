package com.gospelanyanwu.spotflowwallet.config;

import com.gospelanyanwu.spotflowwallet.dto.response.ErrorResponse;
import com.gospelanyanwu.spotflowwallet.exception.InsufficientBalanceException;
import com.gospelanyanwu.spotflowwallet.exception.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException e) {
        return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException e) {
        return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(SpotflowApiException.class)
    public ResponseEntity<ErrorResponse> handleSpotflowApiException(SpotflowApiException e) {
        return errorResponse(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    private ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, String message) {
        ErrorResponse body = new ErrorResponse(status.value(), message, Instant.now());
        return ResponseEntity.status(status).body(body);
    }
}
