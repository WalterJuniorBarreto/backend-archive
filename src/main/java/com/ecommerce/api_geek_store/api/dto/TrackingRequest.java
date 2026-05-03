package com.ecommerce.api_geek_store.api.dto;

public record TrackingRequest(
        String trackingNumber,
        String courierName
) {}