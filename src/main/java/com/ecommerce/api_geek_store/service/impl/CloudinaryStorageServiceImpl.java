package com.ecommerce.api_geek_store.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ecommerce.api_geek_store.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageServiceImpl.class);

    private final Cloudinary cloudinary;


    @Value("${cloudinary.folder.root:geek_store_uploads}")
    private String rootFolder;

    public CloudinaryStorageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("No se puede subir un archivo vacío.");
        }

        String originalFilename = file.getOriginalFilename();
        log.info("Iniciando subida de imagen: {} (Tamaño: {} bytes)", originalFilename, file.getSize());

        try {

            String carpetaDestino = rootFolder + "/productos";

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", carpetaDestino
            ));

            String secureUrl = uploadResult.get("secure_url").toString();

            log.info("Imagen subida exitosamente: {}", secureUrl);

            return secureUrl;

        } catch (IOException e) {
            log.error("Fallo crítico al subir imagen a Cloudinary: {}", originalFilename, e);
            throw new RuntimeException("Error al subir la imagen al servidor de archivos.", e);
        }
    }
}