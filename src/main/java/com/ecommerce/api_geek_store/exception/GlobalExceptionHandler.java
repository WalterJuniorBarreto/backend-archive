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

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<MessageResponse> handleTokenValidationException(TokenValidationException ex) {

        // 1. Log silencioso para DevOps (usamos warn porque no es un fallo del servidor, es error de usuario)
        log.warn("Validación de token fallida: {}", ex.getMessage());

        // 2. Empaquetamos el mensaje en nuestro DTO estandarizado
        MessageResponse response = new MessageResponse(ex.getMessage());

        // 3. Devolvemos el 400 Bad Request con el JSON limpio
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Recurso No Encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler({EmailAlreadyExistsException.class, IllegalStateException.class})
    public ProblemDetail handleConflictExceptions(RuntimeException ex, WebRequest request) {
        log.warn("Conflicto de estado o datos: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.CONFLICT, "Conflicto de Datos", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex, WebRequest request){
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Error de validación en input: {}", errors);

        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Error de Validación",
                "La petición contiene datos inválidos o incompletos",
                request
        );
        problemDetail.setProperty("invalid_params", errors);

        return problemDetail;
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

    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create("https://api.archive.com/errors/" + status.name().toLowerCase()));

        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("path", request.getDescription(false).replace("uri=", ""));

        return problemDetail;
    }
}