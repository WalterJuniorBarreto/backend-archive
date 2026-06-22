package com.ecommerce.api_geek_store.api.dto.response;

import java.math.BigDecimal;

public record ProductVariantResponse(
        Long id,
        String sku,
        String color,
        String talla,
        Integer stock,
        BigDecimal precioAdicional
) { }
