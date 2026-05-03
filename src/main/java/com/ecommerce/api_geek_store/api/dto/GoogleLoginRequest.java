package com.ecommerce.api_geek_store.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank String token
) {}