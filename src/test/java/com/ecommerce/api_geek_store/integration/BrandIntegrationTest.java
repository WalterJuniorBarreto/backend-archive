package com.ecommerce.api_geek_store.integration;

import com.ecommerce.api_geek_store.api.dto.request.BrandRequest;
import com.ecommerce.api_geek_store.domain.model.Brand;
import com.ecommerce.api_geek_store.domain.repository.BrandRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class BrandIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ecommerce_test")
            .withUsername("test")
            .withPassword("test");


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        brandRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe crear una marca en PostgreSQL y retornar 201 Created")
    @WithMockUser(roles = "ADMIN")
    void createBrand_Success() throws Exception {
        BrandRequest request = new BrandRequest("Gucci");

        mockMvc.perform(post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Gucci"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("Debe retornar 400 Bad Request cuando el nombre es muy corto")
    @WithMockUser(roles = "ADMIN")
    void createBrand_ValidationError() throws Exception {
        BrandRequest invalidRequest = new BrandRequest("A");

        mockMvc.perform(post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    @DisplayName("Debe listar las marcas paginadas correctamente")
    @WithMockUser
    void getAllBrands_Success() throws Exception {
        brandRepository.save(new Brand("Amiri"));
        brandRepository.save(new Brand("Adidas"));

        mockMvc.perform(get("/api/v1/brands?statusFilter=ACTIVOS&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].nombre").value("Adidas"))
                .andExpect(jsonPath("$.content[1].nombre").value("Amiri"));
    }

    @Test
    @DisplayName("Debe hacer un Soft Delete y reflejarse en la base de datos")
    @WithMockUser(roles = "ADMIN")
    void deleteBrand_SoftDelete() throws Exception {
        Brand savedBrand = brandRepository.save(new Brand("Nike"));

        mockMvc.perform(delete("/api/v1/brands/{id}", savedBrand.getId()))
                .andExpect(status().isNoContent());

        Brand brandInDb = brandRepository.findById(savedBrand.getId()).orElseThrow();
        assertFalse(brandInDb.getActivo(), "La marca debería estar inactiva en la base de datos");
    }
}