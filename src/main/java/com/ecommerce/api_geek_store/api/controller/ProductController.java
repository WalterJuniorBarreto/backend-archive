package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.request.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.response.ProductResponse;
import com.ecommerce.api_geek_store.api.dto.request.ProductSearchCriteria;
import com.ecommerce.api_geek_store.service.ProductService;
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
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "PRoduct catalog management and retrievel endpoints")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Get all products with dynamic filters",
            description = "Retrieves a paginated list of products based on multiple dynamic criteria such as search term, categories, brands, price range, and gender",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters provided")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Valid @ParameterObject ProductSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 50) Pageable pageable) {

        log.info("Listando productos");

        Page<ProductResponse> products = productService.findAllWithFilters(criteria, pageable);
        return ResponseEntity.ok(products);
    }


    @Operation(
            summary = "Get Product BY id (ADMIN)",
            description = "Retrieves the full details of a product including its draft or archived status. Requires ADMIN role",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id) {

        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create a new product",
            description = "Creates a new sneakers product in draft mode. Requires ADMIN role",
            security = { @SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created successfully",
                    headers = @Header(name = "Location", description = "URI of the newly created product")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation errors  in payload",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Product name already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request){
        log.info("Intento de creacion de producto: {}", request.nombre());

        ProductResponse response = productService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        log.info("Producto creado exitosamente con ID: {}", response.id());

        return ResponseEntity.created(location).body(response);
    }


    @Operation(
            summary = "Actualizar producto base",
            description = "Modifica los datos principales de una zapatilla (nombre, precio, etc.). No afecta stock ni imágenes. Requiere rol ADMIN.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación en los campos enviados",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (No autorizado)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: El nuevo nombre ya existe",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID del producto a modificar", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request){

        ProductResponse response = productService.updateProduct(id, request);

        return ResponseEntity.ok(response);
    }



    @Operation(
            summary = "Archivar producto (Soft Delete)",
            description = "Oculta la zapatilla del catálogo público sin destruir sus registros en la base de datos para mantener la integridad de compras pasadas. Requiere ADMIN.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto archivado exitosamente (Sin contenido)"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (No autorizado)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> archiveProduct(
            @Parameter(description = "ID del producto a archivar", required = true)
            @PathVariable Long id) {

        productService.archiveProduct(id);

        return ResponseEntity.noContent().build();
    }



    @Operation(
            summary = "Publicar producto (Go Live)",
            description = "Cambia el estado de DRAFT a ACTIVE, haciéndolo visible en la tienda pública. Falla si el producto no tiene stock configurado. Requiere ADMIN.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto publicado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de regla de negocio (Ej: No tiene stock)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Void> publishProduct(
            @Parameter(description = "ID del producto a publicar", required = true)
            @PathVariable Long id) {

        productService.publishProduct(id);

        return ResponseEntity.noContent().build();
    }



    @Operation(
            summary = "Desactivar producto (Unpublish/Pause)",
            description = "Cambia el estado a INACTIVE. Oculta el producto de la tienda pública, pero lo mantiene visible en el panel de administración. Requiere ADMIN.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto desactivado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de estado (Ej: El producto está archivado)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateProduct(
            @Parameter(description = "ID del producto a desactivar", required = true)
            @PathVariable Long id) {

        productService.deactivateProduct(id);

        return ResponseEntity.noContent().build();
    }

}

