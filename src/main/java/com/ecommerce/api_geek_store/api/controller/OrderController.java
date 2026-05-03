package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.OrderRequest;
import com.ecommerce.api_geek_store.api.dto.OrderResponse;
import com.ecommerce.api_geek_store.api.dto.TrackingRequest;
import com.ecommerce.api_geek_store.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderController(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }


    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creando orden automática para usuario: {}", userDetails.getUsername());
        OrderResponse orderResponse = orderService.createOrder(orderRequest, userDetails);
        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }


    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Consultando historial de órdenes para: {}", userDetails.getUsername());
        return ResponseEntity.ok(orderService.findMyOrders(userDetails));
    }


    @GetMapping("/admin/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Admin consultando todas las órdenes del sistema");
        return ResponseEntity.ok(orderService.findAllOrders());
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        log.info("Cambiando estado de orden ID: {} a {}", id, status);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }


    @PutMapping("/{id}/tracking")
    public ResponseEntity<OrderResponse> addTrackingInfo(
            @PathVariable Long id,
            @Valid @RequestBody TrackingRequest request
    ) {
        log.info("Agregando tracking a orden ID: {}: {} ({})", id, request.trackingNumber(), request.courierName());
        OrderResponse response = orderService.addTrackingInfo(id, request.trackingNumber(), request.courierName());
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "/create-manual", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrderResponse> createManualOrder(
            @RequestPart("order") String orderJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws Exception {

        log.info("Procesando pago manual para usuario: {}", userDetails.getUsername());

        OrderRequest orderRequest;
        try {
            orderRequest = objectMapper.readValue(orderJson, OrderRequest.class);
        } catch (Exception e) {
            log.error("Error al parsear el JSON de la orden manual: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        OrderResponse response = orderService.createManualOrder(orderRequest, file, userDetails);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}