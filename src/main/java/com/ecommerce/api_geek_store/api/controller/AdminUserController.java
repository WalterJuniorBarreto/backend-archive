package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.AdminUserRequest;
import com.ecommerce.api_geek_store.api.dto.AdminUserUpdateRequest;
import com.ecommerce.api_geek_store.api.dto.UserResponse;
import com.ecommerce.api_geek_store.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")


public class AdminUserController {

    private final UserService userService;


    @GetMapping
    //RequestParam es capturar un parametro de la url
    //required = false no es obligatorio
    @PreAuthorize("hasAnyRole('ADMIN', 'SOPORTE')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@RequestParam(required = false) String keyword, @PageableDefault(size = 30, sort = "email") Pageable pageable, @AuthenticationPrincipal UserDetails authUser) {
        log.info("Usuario '{}' listando la base de datos", authUser.getUsername());
        return ResponseEntity.ok(userService.findAll(keyword, pageable, authUser));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOPORTE')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, @AuthenticationPrincipal UserDetails adminDetails, @RequestHeader(value = "X-Motivo-Accion", required = false) String motivo) {
        String safeMotivo = (motivo != null && !motivo.isBlank()) ? motivo.trim() : null;
        log.info("Auditoría: Admin '{}' consultando detalles del usuario ID: {}", adminDetails.getUsername(), id);
        return ResponseEntity.ok(userService.findById(id, adminDetails, safeMotivo));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SOPORTE')")
    public ResponseEntity<UserResponse> createUser(@AuthenticationPrincipal UserDetails adminDetails, @Valid @RequestBody AdminUserRequest request, @RequestHeader(value = "X-Motivo-Accion", required = false) String motivo) {
        String safeMotivo = (motivo != null && !motivo.isBlank()) ? motivo.trim() : null;
        UserResponse newUser = userService.create(request, adminDetails, safeMotivo);
        log.info("Auditoría: Admin/Soporte '{}' creó un nuevo usuario con email '{}' (ID generado: {})",
                adminDetails.getUsername(), request.email(), newUser.id());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newUser.id())
                .toUri();

        return ResponseEntity.created(location).body(newUser);
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOPORTE')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails adminDetails, @Valid @RequestBody AdminUserUpdateRequest request, @RequestHeader(value = "X-Motivo-Accion", required = false) String motivo) {
        String safeMotivo = (motivo != null && !motivo.isBlank()) ? motivo.trim() : null;
        log.info("Auditoría: Admin/Soporte '{}' está intentando actualizar el perfil del usuario ID: {}",
                adminDetails.getUsername(), id);
        return ResponseEntity.ok(userService.update(id, request, adminDetails, motivo));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOPORTE')")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long id, @AuthenticationPrincipal UserDetails adminDetails, @RequestHeader(value = "X-Motivo-Accion", required = false) String motivo) {
        String safeMotivo = (motivo != null && !motivo.isBlank()) ? motivo.trim() : null;

        log.warn("Auditoría: Admin/Soporte '{}' cambiando el estado (Activo/Inactivo) del usuario ID: {}",
                adminDetails.getUsername(), id);

        userService.toggleStatus(id, adminDetails, safeMotivo);

        return ResponseEntity.noContent().build();
    }




}