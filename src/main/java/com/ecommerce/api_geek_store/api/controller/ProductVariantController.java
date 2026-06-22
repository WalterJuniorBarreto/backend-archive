package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.request.ProductVariantRequest;
import com.ecommerce.api_geek_store.api.dto.response.ProductVariantResponse;
import com.ecommerce.api_geek_store.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/products/{productId}/variants")
@RequiredArgsConstructor
@Tag(name = "Product Variants", description = "Endpoints para la gestion de tallas y stock de zapatillas")
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @Operation(
            summary = "Create a new product",
            description = "Creates a new sneakers product in draft mode. Requires ADMIN role",
            security = { @SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Variante creada exitosamente",
                    headers = @Header(name = "Location", description = "URI al crear una nueva variante")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado - No es admin",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto padre no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVariantResponse> addVariant(
            @Parameter(description = "ID del producto padre", required = true)
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantRequest request){

        ProductVariantResponse response = productVariantService.addVariantToProduct(productId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }


    @Operation(summary = "Actualizar variante y stock",
            description = "Modifica una talla/color y su cantidad en almacén. Requiere ADMIN.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Variante actualizada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. stock negativo)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "403", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "404", description = "Variante o Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "409", description = "Conflicto de pertenencia (IDOR detectado)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PutMapping("/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVariantResponse> updateVariant(
            @Parameter(description = "ID del producto padre", required = true)
            @PathVariable Long productId,
            @Parameter(description = "ID de la variante a modificar", required = true)
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantRequest request) {

        ProductVariantResponse response = productVariantService.updateVariant(productId, variantId, request);
        return ResponseEntity.ok(response);
    }
}
