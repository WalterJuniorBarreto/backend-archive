package com.ecommerce.api_geek_store.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    //subir archivo a la nube  y retornar la url publica
    String uploadFile(MultipartFile file, String folder);
    //elimina un archivo de la nube usando la url
    void deleteFile(String fileUrl);
}