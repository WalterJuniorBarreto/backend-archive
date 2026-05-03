package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.PaymentRequest;
import com.ecommerce.api_geek_store.service.OrderService;
import com.ecommerce.api_geek_store.service.payment.PaymentService;
import com.mercadopago.resources.payment.Payment;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @PostMapping("/process_payment")
    public ResponseEntity<?> processPayment(@RequestBody @Valid PaymentRequest request) {
        log.info("Iniciando proceso de pago para el usuario: {} por un monto de: {}",
                request.payerEmail(), request.transactionAmount());

        Payment payment = paymentService.processPayment(request);

        if ("approved".equals(payment.getStatus())) {
            log.info("Pago aprobado por Mercado Pago. ID Transacción: {}", payment.getId());

            try {
                orderService.createOrderFromPayment(
                        request.payerEmail(),
                        request.items(),
                        request.direccion(),
                        request.transactionAmount()
                );

                log.info("Orden creada exitosamente para el pago: {}", payment.getId());

            } catch (Exception e) {

                log.error("ERROR CRÍTICO: Cobro realizado (MP_ID: {}) pero falló el registro de la orden. Causa: {}",
                        payment.getId(), e.getMessage());
                return ResponseEntity.ok(Map.of(
                        "status", payment.getStatus(),
                        "id", payment.getId().toString(),
                        "order_status", "pending_sync",
                        "message", "Pago procesado, estamos validando tu orden."
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "status", payment.getStatus(),
                    "id", payment.getId().toString(),
                    "status_detail", payment.getStatusDetail()
            ));
        } else {
            log.warn("Pago rechazado/pendiente para {}. Estado: {}, Detalle: {}",
                    request.payerEmail(), payment.getStatus(), payment.getStatusDetail());

            return ResponseEntity.badRequest().body(Map.of(
                    "status", payment.getStatus(),
                    "status_detail", payment.getStatusDetail(),
                    "message", "El pago no pudo ser procesado."
            ));
        }
    }
}