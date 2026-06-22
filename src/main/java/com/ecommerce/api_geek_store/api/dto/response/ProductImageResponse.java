package com.ecommerce.api_geek_store.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representacion de una imagen vinculada a una zapatilla")
public record ProductImageResponse(
        Long id,

        @Schema(description = "URL pública de la imagen (AWS S3 o Placeholder)", example = "https://via.placeholder.com/500")
        String url,

        @Schema(description = "Posición de la imagen (1 = Principal)", example = "1")
        Integer orden
) { }
