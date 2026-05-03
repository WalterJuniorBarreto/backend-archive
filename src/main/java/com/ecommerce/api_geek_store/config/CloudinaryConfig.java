package com.ecommerce.api_geek_store.config;

import com.cloudinary.Cloudinary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {

        if (cloudName == null || apiKey == null || apiSecret == null ||
                cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {

            log.error("ERROR CRÍTICO: Faltan las credenciales de Cloudinary en application.properties o Variables de Entorno.");
            throw new IllegalStateException("Cloudinary no está configurado correctamente.");
        }

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);

        log.info("Cloudinary configurado correctamente para la cuenta: {}", cloudName);

        return new Cloudinary(config);
    }
}