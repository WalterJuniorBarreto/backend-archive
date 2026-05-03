package com.ecommerce.api_geek_store.api.dto;


import com.ecommerce.api_geek_store.domain.model.TipoReclamo;

import java.time.LocalDateTime;




public record ReclamacionesResponse(
        Long id,
        String codigoReclamacion,
        LocalDateTime fechaCreacion,
        String nombreCompleto,
        String emailContacto,
        TipoReclamo tipoReclamo,
        String detalleProblema,
        String pedidoConsumidor,
        String dni,
        String telefono,
        String direccion,
        Boolean resuelto
) {}