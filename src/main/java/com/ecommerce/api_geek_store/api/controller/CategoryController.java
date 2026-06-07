package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.CategoryRequest;
import com.ecommerce.api_geek_store.api.dto.CategoryResponse;
import com.ecommerce.api_geek_store.domain.model.enums.CategoryStatus;
import com.ecommerce.api_geek_store.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.amqp.AbstractRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Endpoints para la gestion de categorias del ecommerce archive")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Obtener todas las categorias",
            description = "Retorna una lista paginada de categorias. Permite busqueda por termino, filtrado por estado y ordenamiento por defecto  nombre asc"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorias obtenidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parametros de busqueda o paginacion invalidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @Parameter(description = "Termino de busqueda para la lupa - Running, Futbol")
            @RequestParam(required = false) String searchTerm,

            @Parameter(description = "FIltro de estado: ACTIVO, INACTIVO o TODOS")
            @RequestParam(required = false) CategoryStatus status,

            @Parameter(description = "Configuracion de paginacion y ordenamiento. Por defecto tamaño 20, ordenado por nombre asc")
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable
            ){
        log.info("REST REQUEST para obtener categorias. Busqueda {}, Estado {}, Paginacion {}", searchTerm, status, pageable);
        Page<CategoryResponse> categories = categoryService.findAll(searchTerm, status, pageable);
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Obtener categoria por ID",
            description = "Retorna los detalles de una categoria especifica en el ecommerce basada en su identificador unico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Categoria no encontrada ID inexistente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "ID Unico de la categoria a buscar", required = true)
            @PathVariable Long id
    ){
        log.info("REST REQUEST para obtener categoria con ID: {}", id);
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @Operation(
            summary = "Crear nueva categoria",
            description = "Registra una nueva categoria en el sistema, valida que los datos de entrada cumplan con las reglas de negocio",
            security = { @SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria creada exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada invalidos Falla de validacion",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto, ya existe una categoria con ese nombre",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest categoryRequest, UriComponentsBuilder uriComponentsBuilder){
        log.info("REST REQUEST para crear una nueva categoria {}", categoryRequest.nombre());

        CategoryResponse category = categoryService.create(categoryRequest);

        URI location = uriComponentsBuilder.path("api/v1/categories/{id}")
                .buildAndExpand(category.id())
                .toUri();
        return ResponseEntity.created(location).body(category);
    }

    @Operation(
            summary = "Actualizar categoria",
            description = "Modifica los datos de una categoria existente. Requiere privilegios de ADMIN",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria actualizada exitosamente",
                    content = @Content(schema = @Schema( implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto: El nuevo nombre ya esta en uso",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "ID unico de la caategoria a actualizar", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest categoryRequest){
        log.info("REST REQUEST para actualizar categoria ID {} con nuevos datos: {}", id, categoryRequest.nombre());
        CategoryResponse categoryActualizada = categoryService.update(id, categoryRequest);
        return ResponseEntity.ok(categoryActualizada);
    }

    @Operation(
            summary = "Desactivar categoria SOFT DELETE",
            description = "Cambia el estado de una categoria a INACTIVO. No elimina el registro fisicamente para preservar la integridad referencial de los productos. Requiere privilegios de ADMIN",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria desactivada exitosamente, sin contenido en la respuesta"),
            @ApiResponse(responseCode = "404", description = "Categoria no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID unico de la categoria a desactivar", required = true)
            @PathVariable Long id){
        log.info("REST REQUEST para desactivar categoria ID: {}", id);
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Activar categoria",
            description = "Restaura una categoria previamente desactivada, cambiando su estado a ACTIVO. Requiere privilegios de ADMIN",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria activada exitosamente, sin contenido en la respuesta"),
            @ApiResponse(responseCode = "404", description = "Categoria no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/${id}/activate")
    public ResponseEntity<Void> activateCategory(
            @Parameter(description = "ID unico de la categoria a activar", required = true)
            @PathVariable Long id
    ){
        log.info("REST REQUEST para activar categoria ID: {}", id);

        categoryService.activate(id);

        return ResponseEntity.noContent().build();
    }
}