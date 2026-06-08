package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.BrandRequest;
import com.ecommerce.api_geek_store.api.dto.BrandResponse;
import com.ecommerce.api_geek_store.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.net.URI;


@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Directorio de marcas", description = "Endpoints para la gestion centralizada del catalogo")
public class BrandController {


    private final BrandService brandService;


    @Operation(
            summary = "Listar marcas paginadas",
            description = "Obtiene una lista de marca del sistema. Permite filtrado dinamico por termino de busqueda y estado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de marcas obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<BrandResponse>> getAllBrands(
            @Parameter(description = "Termino para buscar por nombre de marca")
            @RequestParam(required = false) String searchTerm,

            @Parameter(description = "FIltro de estado logico (ACTIVOS, INACTIVOS, TODOS", example = "ACTIVOS")
            @RequestParam(defaultValue = "ACTIVOS") String statusFilter,

            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("REST REQUEST - Obtener marca - Searc: '{}' - Status: '{}' - Page: {}", searchTerm, statusFilter, pageable.getPageNumber());
        Page<BrandResponse> brands = brandService.findAll(searchTerm, statusFilter, pageable);
        return ResponseEntity.ok(brands);
    }

    @Operation(
            summary = "Obtener marca por ID",
            description = "Devuelve los detalles exactos de una marca registrada basándose en su identificador único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Marca encontrada"),
            @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(
            @Parameter(description = "ID unico de la marca", example = "1")
            @PathVariable("id") @Positive(message = "El ID de la marca debe ser mayor a cero") Long id) {
        log.debug("REST Request: Intentado obtener la marca con ID: {}", id);

        return ResponseEntity.ok(brandService.findById(id));
    }

    @Operation(
            summary = "Crear nueva marca",
            description = "Registra una nueva marca en el catálogo. Requiere privilegios de Administrador.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Marca creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validacion en los datos enviados",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Fallo de Autenticación (Token inválido o ausente)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Acceso Denegado (No es ADMIN)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: Ya existe una marca con ese nombre",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request, UriComponentsBuilder uriComponentsBuilder) {
        log.info("REST Request: Intentando crear nueva marca: {}", request.nombre());
        BrandResponse response = brandService.create(request);
        URI location = uriComponentsBuilder.path("/api/v1/brands/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Actualizar marca existente",
            description = "Modifica los datos de una marca. Requiere privilegios de Administrador.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Marca actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Marca no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto: Nombre ya en uso")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> updateBrand(
            @Parameter(description = "ID de la marca a actualizar")
            @PathVariable("id") @Positive(message = "El ID debe ser valido") Long id, @Valid @RequestBody BrandRequest request) {
        log.info("REST REQUEST: Admin actualizando marca ID: {} a nuevo nombre: {}", id, request.nombre());
        return ResponseEntity.ok(brandService.update(id, request));
    }

    @Operation(
            summary = "Desactivar marca (Soft Delete)",
            description = "Oculta lógicamente una marca del catálogo principal. No elimina el registro físicamente.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Marca desactivada correctamente (Sin contenido)"),
            @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBrand(
            @Parameter(description = "ID de la marca a desactivar")
            @PathVariable("id") @Positive(message = "EL ID debe ser mayor a cero") Long id) {
        log.info("REST REQUEST: Admin solicitando eliminacion de marca  ID {}", id);
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reactivar marca",
            description = "Restaura una marca previamente desactivada para que vuelva a ser visible en el catálogo.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Marca reactivada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La marca ya se encontraba activa"),
            @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    })
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activarBrand(
            @Parameter(description = "ID de la marca a reactivar")
            @PathVariable("id") @Positive(message = "El ID debe ser mayor a cero") Long id){
        log.info("REST REQUEST - Admin solicitando activacion de marca ID: {}", id);
        brandService.activar(id);
        return ResponseEntity.noContent().build();
    }



}