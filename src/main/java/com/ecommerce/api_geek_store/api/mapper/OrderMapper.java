package com.ecommerce.api_geek_store.api.mapper;

import com.ecommerce.api_geek_store.api.dto.OrderItemResponse;
import com.ecommerce.api_geek_store.api.dto.OrderResponse;
import com.ecommerce.api_geek_store.domain.model.Order;
import com.ecommerce.api_geek_store.domain.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        if (item == null) return null;

        return new OrderItemResponse(
                item.getId(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getProduct() != null ? item.getProduct().getId() : null,

                item.getNombreProducto() != null ? item.getNombreProducto() : "Producto sin nombre",
                item.getColor(),
                item.getTalla()
        );
    }

    public OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;

        return new OrderResponse(
                order.getId(),
                order.getFechaCreacion(),
                order.getEstado(),
                order.getTotal(),
                order.getUser() != null ? order.getUser().getEmail() : "Usuario Desconocido",
                order.getItems() != null ? order.getItems().stream()
                        .map(this::toOrderItemResponse)
                        .collect(Collectors.toList()) : Collections.emptyList(),
                order.getTrackingNumber(),
                order.getCourierName(),
                order.getMetodoPago(),
                order.getCodOperacion(),
                order.getUrlComprobante(),
                order.getEnvio() != null ? order.getEnvio().toString() : "Recojo en tienda"
        );
    }
}