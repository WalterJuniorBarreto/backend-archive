package com.ecommerce.api_geek_store.service.payment;

import com.ecommerce.api_geek_store.api.dto.PaymentRequest;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Value("${mercadopago.access_token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new RuntimeException("FATAL: El Token de Mercado Pago no está configurado.");
        }
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("Servicio de Pagos inicializado con Mercado Pago.");
    }

    public Payment processPayment(PaymentRequest request) {

        String idempotencyKey = UUID.randomUUID().toString();

        try {
            PaymentClient client = new PaymentClient();

            BigDecimal amount = request.transactionAmount()
                    .setScale(2, RoundingMode.HALF_UP);

            PaymentPayerRequest payerRequest = PaymentPayerRequest.builder()
                    .email(request.payerEmail())

                    .build();

            PaymentCreateRequest createRequest = PaymentCreateRequest.builder()
                    .transactionAmount(amount)
                    .token(request.token())
                    .description("Geek Store Order")
                    .installments(request.installments())
                    .paymentMethodId(request.paymentMethodId())
                    .payer(payerRequest)
                    .build();

            Map<String, String> customHeaders = new HashMap<>();
            customHeaders.put("X-Idempotency-Key", idempotencyKey);

            MPRequestOptions options = MPRequestOptions.builder()
                    .customHeaders(customHeaders)
                    .build();

            log.info("Iniciando proceso de pago para: {} | Monto: {}", request.payerEmail(), amount);

            Payment payment = client.create(createRequest, options);

            log.info("Pago procesado exitosamente. Estado: {} | ID: {}", payment.getStatus(), payment.getId());

            return payment;

        } catch (MPApiException ex) {
            log.error("Error API Mercado Pago. Status: {} | Detalle: {}", ex.getStatusCode(), ex.getApiResponse().getContent());

            throw new RuntimeException("El pago fue rechazado por la procesadora. Verifique los datos de su tarjeta.");

        } catch (MPException ex) {
            log.error("Error de conexión con Mercado Pago: {}", ex.getMessage());
            throw new RuntimeException("Hubo un problema de comunicación con el banco. Intente nuevamente.");

        } catch (Exception e) {
            log.error("Error interno inesperado en PaymentService: ", e);
            throw new RuntimeException("Ocurrió un error interno al procesar el pago.");
        }
    }
}