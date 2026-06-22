package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsS3StorageServiceImpl implements StorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.endpoint-url}")
    private String endpointUrl;


    @Override
    public String uploadFile(MultipartFile file, String folder){
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        //images/1283092138-90213-091231.jpg
        String uniqueFileName = folder + "/" + UUID.randomUUID() + extension;

        try{
            //configuracion de subida a aws s3
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .contentType(file.getContentType())
                    .build();

            //subimos el archivo en la nube en s3
            s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Archivo subido a S3 con exito: {}", uniqueFileName);

            return endpointUrl + uniqueFileName;

        } catch (IOException e){
            log.error("Error  procesando el archivo para s3: {}", e.getMessage());
            throw new RuntimeException("Fallo al subir archivo a la nube");
        }
    }


    @Override
    public void deleteFile(String fileUrl){
        try{
            String key = fileUrl.replace(endpointUrl, "");

            //configuracion para borrar archivo en aws s3
            DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(delReq);
            log.info("Archivo eliminaod de S3: {}", key);
        } catch (Exception e){
            log.error("NO se pudo eliminar el archivo en S3: {}", e.getMessage());
        }
    }
}
