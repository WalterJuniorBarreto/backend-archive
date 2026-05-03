package com.ecommerce.api_geek_store.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        LocalDateTime fechaCreacion,
        String estado,
        BigDecimal total,
        String userEmail,
        List<OrderItemResponse> items,
        String trackingNumber,
        String courierName,
        String metodoPago,
        String codOperacion,
        String urlComprobante,
        String direccionEnvio
) {}