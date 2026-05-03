package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private static final Logger log = LoggerFactory.getLogger(MediaController.class);
    private final StorageService storageService;

    public MediaController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo no puede estar vacío"));
        }

        log.info("Iniciando subida de archivo: {} (Tipo: {})", file.getOriginalFilename(), file.getContentType());

        try {
            String url = storageService.uploadFile(file);
            log.info("Archivo subido exitosamente. URL: {}", url);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            log.error("Error crítico al subir archivo a Cloudinary: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al procesar el archivo multimedia"));
        }
    }
}