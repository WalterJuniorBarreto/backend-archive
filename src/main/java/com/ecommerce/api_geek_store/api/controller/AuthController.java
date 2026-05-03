package com.ecommerce.api_geek_store.api.controller;


import com.ecommerce.api_geek_store.api.dto.*;
import com.ecommerce.api_geek_store.service.AuthService;
import com.ecommerce.api_geek_store.util.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;


    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {

        UserResponse response = authService.register(registerRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/api/users/{id}")
                .buildAndExpand(response.id())
                .toUri();
        log.info("Usuario registrado exitosamente con ID interno: {}", response.id());
        return ResponseEntity.created(location).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Intento de login para usuario: {}", maskEmail(loginRequest.email()));
        AuthResponse authResponse = authService.login(loginRequest);
        ResponseCookie jwtCookie = cookieUtil.createJwtCookie(authResponse.token());
        log.info("Login exitoso");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(authResponse);
    }


    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody @Valid GoogleLoginRequest request) {
        log.info("Intento de Login via Google OAuth");

        AuthResponse authResponse = authService.loginWithGoogle(request.token());
        ResponseCookie jwtCookie = cookieUtil.createJwtCookie(authResponse.token());
        log.info("Login vía Google exitoso. Cookie de sesión interna generada.");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(authResponse);
    }


    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        log.info("Procesando solicitud de cierre de sesión...");

        ResponseCookie deleteCookie = cookieUtil.deleteJwtCookie();
        log.info("Sesión cerrada. Cookie JWT destruida.");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(new MessageResponse("Sesión cerrada correctamente"));
    }

    @GetMapping("/confirm")
    public ResponseEntity<MessageResponse> confirmAccount(@RequestParam("token") String token) {
        log.info("Procesando solicitud de confirmación de cuenta con token...");
        authService.confirmToken(token);
        log.info("Cuenta confirmada exitosamente en la base de datos.");
        return ResponseEntity.ok(
                new MessageResponse("Cuenta confirmada exitosamente. Ya puedes iniciar sesión.")
        );
    }



    @PostMapping("/password/recover")
    public ResponseEntity<MessageResponse> recoverPassword(@Valid @RequestBody PasswordRecoverRequest request) {
        String maskedEmail = maskEmail(request.email());
        log.info("Procesando solicitud de recuperación de contraseña para: {}", maskedEmail);
        authService.sendRecoveryCode(request.email());
        return ResponseEntity.ok(
                new MessageResponse("Si el correo electrónico ingresado está registrado, recibirás un código de recuperación en unos minutos.")
        );
    }

    @PostMapping("/verify-recovery-code")
    public ResponseEntity<MessageResponse> verifyRecoveryCode(@Valid @RequestBody CodeVerificationRequest request) {
        String maskedEmail = maskEmail(request.email());
        log.info("Iniciando verificación de código OTP para el usuario: {}", maskedEmail);
        authService.validateRecoveryCode(request.email(), request.code());
        log.info("Código OTP validado exitosamente para: {}", maskedEmail);
        return ResponseEntity.ok(
                new MessageResponse("Código verificado correctamente. Puedes ingresar tu nueva contraseña.")
        );
    }

    @PostMapping("/password/reset")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        String maskedEmail = maskEmail(request.email());
        log.info("Iniciando restablecimiento definitivo de contraseña para: {}", maskedEmail);
        authService.resetPassword(request);
        log.info("Contraseña actualizada exitosamente en la base de datos para: {}", maskedEmail);
        return ResponseEntity.ok(
                new MessageResponse("Tu contraseña ha sido actualizada correctamente. Ya puedes iniciar sesión en ARCHIVE.")
        );
    }

    private String maskEmail(String email){
        if(email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) return "***@" + domain;
        return name.charAt(0) + "***" + name.charAt(name.length() -  1) + "@" + domain;
    }

}