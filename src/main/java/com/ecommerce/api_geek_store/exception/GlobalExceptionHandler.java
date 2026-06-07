package com.ecommerce.api_geek_store.exception;

import com.ecommerce.api_geek_store.api.dto.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🛡️ 1. Unificamos el TokenValidationException al estándar ProblemDetail
    @ExceptionHandler(TokenValidationException.class)
    public ProblemDetail handleTokenValidationException(TokenValidationException ex, WebRequest request) {
        log.warn("Validación de token fallida: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Token Inválido", ex.getMessage(), request);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Recurso No Encontrado", ex.getMessage(), request);
    }

    // ✅ Nivel Senior: Excepción agregada al arreglo
    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            IllegalStateException.class,
            DuplicateResourceException.class // <-- ¡ESTO ES LO QUE FALTA!
    })
    public ProblemDetail handleConflictExceptions(RuntimeException ex, WebRequest request) {
        log.warn("Conflicto de estado o datos: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.CONFLICT, "Conflicto de Datos", ex.getMessage(), request);
    }

    // 🚀 BLINDAJE ENTERPRISE: Interceptor Absoluto de Validaciones
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException ex, WebRequest request){

        // 1. Extraemos el mensaje real de tu anotación (@Size, @Pattern, etc.)
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getDefaultMessage())
                .findFirst() // Tomamos el primer error detectado
                .orElse("Datos inválidos en el formulario");

        // 2. DevOps: Dejamos rastro en la consola de Spring Boot
        log.warn("Validación interceptada con éxito. Enviando al frontend: {}", errorMsg);

        // 3. Inyectamos el mensaje directamente en el "detail" para Next.js
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validación Fallida",
                errorMsg, // 🪄 Magia: Aquí viaja el "El nombre debe tener entre 2 y 50 caracteres"
                request
        );
    }
    @ExceptionHandler({
            InsufficientStockException.class,
            InvalidPasswordException.class,
            IllegalArgumentException.class
    })
    public ProblemDetail handleBusinessLogicException(RuntimeException ex, WebRequest request) {
        log.warn("Error de negocio: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Petición Inválida", ex.getMessage(), request);
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ProblemDetail handleAuthExceptions(Exception ex, WebRequest request) {
        log.warn("Fallo de autenticación: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.UNAUTHORIZED, "Fallo de Autenticación", ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Acceso denegado a recurso protegido: {}", request.getDescription(false));
        return buildProblemDetail(
                HttpStatus.FORBIDDEN,
                "Acceso Denegado",
                "No tienes los permisos necesarios para realizar esta acción.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(Exception ex, WebRequest request) {
        log.error("ERROR CRÍTICO NO CONTROLADO: ", ex);
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error Interno del Servidor",
                "Ocurrió un error inesperado. Por favor, contacte a soporte si el problema persiste.",
                request
        );
    }



    // 🏛️ Helper method perfectamente construido
    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        // Excelente detalle el URI personalizado
        problemDetail.setType(URI.create("https://api.archive.com/errors/" + status.name().toLowerCase()));

        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }
}