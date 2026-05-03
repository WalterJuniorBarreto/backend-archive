package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.*;
import com.ecommerce.api_geek_store.api.mapper.UserMapper;
import com.ecommerce.api_geek_store.domain.model.*;
import com.ecommerce.api_geek_store.domain.repository.ConfirmationTokenRepository;
import com.ecommerce.api_geek_store.domain.repository.PasswordResetTokenRepository;
import com.ecommerce.api_geek_store.domain.repository.UserRepository;
import com.ecommerce.api_geek_store.exception.CodeValidationException;
import com.ecommerce.api_geek_store.exception.EmailAlreadyExistsException;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.exception.TokenValidationException;
import com.ecommerce.api_geek_store.service.AuthService;
import com.ecommerce.api_geek_store.service.jwt.JwtService;
import com.ecommerce.api_geek_store.service.notification.EmailService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final UserDetailsService userDetailsService;
    private final ConfirmationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordTokenRepository;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;


    @Value("${app.security.token.expiration-minutes:15}")
    private int tokenExpirationMinutes;
    @Value("${archive.security.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;




    @Value("${google.client.id}")
    private String googleClientId;


    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String maskedEmail = maskEmail(request.email());
        log.info("Iniciando registro de usuario: {}", maskedEmail);

        User user = userRepository.findByEmail(request.email())
                .map(existingUser -> updateUnconfirmedUser(existingUser, request))
                .orElseGet(() -> createNewUser(request));
        User savedUser = userRepository.save(user);
        ConfirmationToken token = new ConfirmationToken(savedUser, tokenExpirationMinutes);
        tokenRepository.save(token);
        emailService.sendVerificationEmail(request.email(), request.nombre(), token.getToken());

        log.info("Usuario registrado exitosamente en BD. Esperando confirmación: {}", maskedEmail);

        return userMapper.toResponse(savedUser);
    }
    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        String maskEmail = maskEmail(loginRequest.email());
        log.info("Procesando login para usuario: {}", maskEmail);
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwtToken = jwtService.generateToken(userDetails);
            log.info("Usuario logueado exitosamente. Token generado para: {}", maskEmail);
            return new AuthResponse(jwtToken);
        } catch (DisabledException e){
            log.warn("Intento de login bloqueado. Cuenta no verificada: {}", maskEmail);
            throw new DisabledException("Tu cuenta no ha sido verificada. Revisa tu correo");
        } catch (BadCredentialsException e){
            log.warn("Fallo de autenticación (credenciales inválidas) para: {}", maskEmail);
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }
    }

    @Override
    @Transactional
    public void confirmToken(String token) {
        ConfirmationToken confirmationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenValidationException("El token proporcionado es inválido o no existe."));
        if (confirmationToken.getConfirmedAt() != null) {
            throw new TokenValidationException("El email ya ha sido confirmado previamente.");
        }
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenValidationException("El enlace de confirmación ha expirado. Solicita uno nuevo.");
        }
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        User user = confirmationToken.getUser();
        user.setEnabled(true);
        tokenRepository.save(confirmationToken);
        String maskedEmail = maskEmail(user.getEmail());
        log.info("Cuenta verificada y activada exitosamente para: {}", maskedEmail);
    }

    @Override
    @Transactional
    public void sendRecoveryCode(String email) {
        String maskedEmail = maskEmail(email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Intento de recuperación para correo no registrado: {}", maskedEmail);
            return;
        }
        User user = userOpt.get();
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            log.warn("Intento de recuperación de contraseña para usuario GOOGLE: {}", maskedEmail);
            return;
        }
        SecureRandom secureRandom = new SecureRandom();
        String code = String.valueOf(100000 + secureRandom.nextInt(900000));
        passwordTokenRepository.findByUser(user).ifPresent(passwordTokenRepository::delete);
        PasswordResetToken token = new PasswordResetToken(code, user, otpExpirationMinutes);
        passwordTokenRepository.save(token);
        emailService.sendRecoveryCodeEmail(user.getEmail(), user.getNombre(), code);
        log.info("Código de recuperación enviado exitosamente a: {}", maskedEmail);
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        String maskedEmail = maskEmail(request.email());
        log.info("Iniciando proceso de cambio de contraseña para: {}", maskedEmail);
        String genericErrorMessage = "El código de verificación es incorrecto o ha expirado.";
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Intento de cambio de clave (Usuario no encontrado) para: {}", maskedEmail);
                    return new CodeValidationException(genericErrorMessage);
                });
        PasswordResetToken token = passwordTokenRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("Intento de cambio de clave (Sin token pendiente) para: {}", maskedEmail);
                    return new CodeValidationException(genericErrorMessage);
                });
        if (token.getExpirationTime().isBefore(LocalDateTime.now())) {
            log.warn("Intento de cambio de clave (Código expirado) para: {}", maskedEmail);
            throw new CodeValidationException(genericErrorMessage);
        }
        boolean isCodeValid = MessageDigest.isEqual(
                token.getCode().getBytes(StandardCharsets.UTF_8),
                request.code().getBytes(StandardCharsets.UTF_8)
        );
        if (!isCodeValid) {
            log.warn("Intento de cambio de clave (Código incorrecto) para: {}", maskedEmail);
            throw new CodeValidationException(genericErrorMessage);
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        passwordTokenRepository.delete(token);
        // 6. Trazabilidad Segura
        log.info("Contraseña restablecida exitosamente y token destruido para: {}", maskedEmail);
    }
    @Override
    @Transactional
    public AuthResponse loginWithGoogle(String token) {
        try {
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(token);
            if (googleIdToken == null) {
                log.warn("Intento de login fallido: Token de Google inválido o alterado.");
                throw new BadCredentialsException("Token de Google inválido.");
            }
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String maskedEmail = maskEmail(email);
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                log.info("Creando nuevo usuario via Google OAuth2: {}", maskedEmail);
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setNombre((String) payload.get("given_name"));
                newUser.setApellido((String) payload.get("family_name"));
                newUser.setRol(Role.ROLE_USER);
                newUser.setEnabled(true);
                newUser.setAuthProvider(AuthProvider.GOOGLE);
                return userRepository.save(newUser);
            });
            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                log.info("Usuario LOCAL vinculado con cuenta GOOGLE exitosamente: {}", maskedEmail);
            }
            UserDetails userDetails = buildUserDetails(user);
            String jwtToken = jwtService.generateToken(userDetails);
            log.info("Autenticación con Google exitosa para: {}", maskedEmail);
            return new AuthResponse(jwtToken);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Fallo de red o seguridad al validar con Google: {}", e.getMessage());
            throw new BadCredentialsException("No se pudo contactar con los servidores de Google");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateRecoveryCode(String email, String code) {
        String maskedEmail = maskEmail(email);
        log.info("Iniciando validación de código de recuperación para: {}", maskedEmail);
        String genericErrorMessage = "El código de verificación es incorrecto o ha expirado.";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Fallo OTP (Usuario no encontrado) para: {}", maskedEmail);
                    return new CodeValidationException(genericErrorMessage);
                });
        PasswordResetToken token = passwordTokenRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.warn("Fallo OTP (Sin solicitud pendiente) para: {}", maskedEmail);
                    return new CodeValidationException(genericErrorMessage);
                });
        if (token.getExpirationTime().isBefore(LocalDateTime.now())) {
            log.warn("Fallo OTP (Código expirado) para: {}", maskedEmail);
            throw new CodeValidationException(genericErrorMessage);
        }
        boolean isCodeValid = MessageDigest.isEqual(
                token.getCode().getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8)
        );
        if (!isCodeValid) {
            log.warn("Fallo OTP (Código incorrecto) para: {}", maskedEmail);
            throw new CodeValidationException(genericErrorMessage);
        }

        log.info("Validación OTP exitosa para: {}", maskedEmail);
    }


    private User updateUnconfirmedUser(User existingUser, RegisterRequest request) {
        if (existingUser.isEnabled()) {
            log.warn("Intento de registro fallido, email ya en uso y confirmado: {}", maskEmail(request.email()));
            throw new EmailAlreadyExistsException("El email ya está en uso y confirmado.");
        }

        existingUser.setNombre(request.nombre());
        existingUser.setApellido(request.apellido());
        existingUser.setPassword(passwordEncoder.encode(request.password()));
        return existingUser;
    }

    private User createNewUser(RegisterRequest request) {
        User newUser = userMapper.toEntity(request);
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRol(Role.ROLE_USER);
        newUser.setEnabled(false);
        newUser.setAuthProvider(AuthProvider.LOCAL);
        return newUser;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) return "***@" + domain;
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + domain;
    }


    private UserDetails buildUserDetails(User user) {
        String roleName = user.getRol() != null ? user.getRol().name() : Role.ROLE_USER.name();
        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(roleName));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                true,
                true,
                true,
                user.isEnabled(),

                authorities

                );

    }



}