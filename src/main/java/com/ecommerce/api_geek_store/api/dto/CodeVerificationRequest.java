package com.ecommerce.api_geek_store.api.dto;

public record CodeVerificationRequest(
        String email,
        String code
) {}