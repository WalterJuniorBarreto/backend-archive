package com.ecommerce.api_geek_store.api.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record PaymentRequest(
        @NotNull String token,
        @NotNull BigDecimal transactionAmount,
        @NotNull String paymentMethodId,
        @NotNull String payerEmail,
        @NotNull Integer installments,
        List<PaymentItem> items,
        @NotNull @Valid
        PaymentAddress direccion
) {
    public record PaymentItem(
            Long productId,
            Long variantId,
            Integer cantidad
    ) {}
    public record PaymentAddress(
            String calle, String ciudad, String estado, String codigoPostal, String pais
    ) {}
}