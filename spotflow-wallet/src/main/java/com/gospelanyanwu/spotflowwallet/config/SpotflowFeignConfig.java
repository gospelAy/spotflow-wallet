package com.gospelanyanwu.spotflowwallet.config;

import feign.Response;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class SpotflowFeignConfig {

    @Value("${spotflow.secret-key}")
    private String secretKey;

    @Bean
    public RequestInterceptor spotflowAuthInterceptor() {
        return requestTemplate -> requestTemplate.header("Authorization", "Bearer " + secretKey);
    }

    @Bean
    public ErrorDecoder spotflowErrorDecoder() {
        return new SpotflowErrorDecoder();
    }

    private static class SpotflowErrorDecoder implements ErrorDecoder {

        @Override
        public Exception decode(String methodKey, Response response) {
            String body = readBody(response);
            return new SpotflowApiException(
                    "Spotflow API call '" + methodKey + "' failed with status " + response.status() + ": " + body);
        }

        private String readBody(Response response) {
            if (response.body() == null) {
                return "";
            }
            try (InputStream bodyStream = response.body().asInputStream()) {
                return new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "";
            }
        }
    }
}
