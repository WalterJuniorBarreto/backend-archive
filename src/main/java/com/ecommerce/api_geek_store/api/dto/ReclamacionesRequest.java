package com.ecommerce.api_geek_store.api.dto;


import com.ecommerce.api_geek_store.domain.model.TipoBien;
import com.ecommerce.api_geek_store.domain.model.TipoReclamo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ReclamacionesRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombreCompleto,

        @NotBlank(message = "El DNI es obligatorio")
        String dni,

        @NotBlank(message = "El teléfono es obligatorio")
        String telefono,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La dirección es obligatoria")
        String direccion,

        @NotNull(message = "El tipo de bien es obligatorio")
        TipoBien tipoBien,

        @NotNull(message = "El monto es obligatorio")
        BigDecimal montoReclamado,

        String descripcionBien,

        @NotNull(message = "El tipo de reclamo es obligatorio")
        TipoReclamo tipoReclamo,

        @NotBlank(message = "El detalle del problema es obligatorio")
        String detalleProblema,

        @NotBlank(message = "El pedido del consumidor es obligatorio")
        String pedidoConsumidor
) {}