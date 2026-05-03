package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.ReclamacionesRequest;
import com.ecommerce.api_geek_store.api.dto.ReclamacionesResponse;
import com.ecommerce.api_geek_store.service.ReclamacionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/complaints")
public class ReclamacionController {

    private static final Logger log = LoggerFactory.getLogger(ReclamacionController.class);
    private final ReclamacionService reclamacionService;

    public ReclamacionController(ReclamacionService reclamacionService) {
        this.reclamacionService = reclamacionService;
    }

    @PostMapping
    public ResponseEntity<ReclamacionesResponse> create(@Valid @RequestBody ReclamacionesRequest request) {
        log.info("Recibido nuevo reclamo de: {}", request.email());
        ReclamacionesResponse response = reclamacionService.registrarReclamo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReclamacionesResponse>> getAll() {
        log.debug("Admin consultando lista completa de reclamaciones");
        return ResponseEntity.ok(reclamacionService.listarTodos());
    }



    @PatchMapping("/{id}/resolver")
    public ResponseEntity<ReclamacionesResponse> markAsResolved(
            @PathVariable Long id,
            @RequestParam Boolean resuelto) {

        log.info("Cambiando estado de reclamo ID: {} a Resuelto: {}", id, resuelto);
        ReclamacionesResponse response = reclamacionService.cambiarEstado(id, resuelto);
        return ResponseEntity.ok(response);
    }
}