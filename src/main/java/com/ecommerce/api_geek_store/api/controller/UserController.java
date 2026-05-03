package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.ChangePasswordRequest;
import com.ecommerce.api_geek_store.api.dto.UserProfileUpdateRequest;
import com.ecommerce.api_geek_store.api.dto.UserResponse;
import com.ecommerce.api_geek_store.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Consulta de perfil para: {}", userDetails.getUsername());
        UserResponse response = userService.getUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }


    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        log.info("Actualización de perfil solicitada por: {}", userDetails.getUsername());
        UserResponse response = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changeMyPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Intento de cambio de contraseña para usuario: {}", userDetails.getUsername());

        userService.changePassword(request, userDetails);

        log.info("Cambio de contraseña exitoso para usuario: {}", userDetails.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "Contraseña actualizada correctamente",
                "status", "success"
        ));
    }
}