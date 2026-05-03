package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest orderRequest, UserDetails userDetails);
    List<OrderResponse> findMyOrders(UserDetails userDetails);
    List<OrderResponse> findAllOrders();
    OrderResponse updateOrderStatus(Long orderId, String status);
    void createOrderFromPayment(
            String email,
            List<PaymentRequest.PaymentItem> items,
            PaymentRequest.PaymentAddress direccion,
            BigDecimal totalPaid
    );
    OrderResponse addTrackingInfo(Long orderId, String trackingNumber, String courierName);
    OrderResponse createManualOrder(OrderRequest req, MultipartFile file, UserDetails userDetails);
}
