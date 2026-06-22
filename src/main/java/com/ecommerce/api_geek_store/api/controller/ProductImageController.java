package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.response.ProductImageResponse;
import com.ecommerce.api_geek_store.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
@Tag(name = "Product Images", description = "Gestion de galeria fotografica de los productos")
public class ProductImageController {

    private final ProductImageService productImageService;

    @Operation(
            summary = "Subir imagen de producto",
            description = "Sube una imagen fisica (jpg/png/webp( y la socia al producto. Requiere ROL ADMIN",
            security = { @SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Imagen subida exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Archivo corrupto o formato incorrecto",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso denegado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageResponse> uploadImage(
            @PathVariable Long productId,
            @Parameter(description = "Archivo fisico de la imagen", required = true)
            @RequestPart("file")MultipartFile file,
            @Parameter(description = "Orden de aparicion 1=principal", example = "1")
            @RequestParam("orden") Integer orden){

        ProductImageResponse response = productImageService.uploadImage(productId, file, orden);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @Operation(summary = "Eliminar imagen de un producto", description = "Borra el archivo físico de la nube y desvincula el registro de la base de datos. Requiere ADMIN.",
            security = { @SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagen eliminada exitosamente (Sin contenido)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (No es Admin)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "404", description = "Imagen no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "409", description = "Conflicto de pertenencia (IDOR)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "ID del producto padre", required = true)
            @PathVariable Long productId,
            @Parameter(description = "ID de la imagen a eliminar", required = true)
            @PathVariable Long imageId){

        productImageService.deleteImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }
}
