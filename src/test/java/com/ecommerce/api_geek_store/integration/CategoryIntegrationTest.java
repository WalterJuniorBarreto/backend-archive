package com.ecommerce.api_geek_store.integration;

import com.ecommerce.api_geek_store.api.dto.request.CategoryRequest;
import com.ecommerce.api_geek_store.domain.model.Category;
import com.ecommerce.api_geek_store.domain.model.enums.CategoryStatus;
import com.ecommerce.api_geek_store.domain.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Test de Integracion: Flujo completo de categorias")
class CategoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;


    @BeforeEach
    void setUp(){
        categoryRepository.deleteAll();
    }


    @Test
    @DisplayName("Debe crear una categoria y persistirla en DB cuando es ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCategoryWhenAdmin() throws Exception{
        CategoryRequest request = new CategoryRequest("Running");

        mockMvc.perform(post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nombre").value("Running"))
                .andExpect(jsonPath("$.status").value("ACTIVO"));
        assertThat(categoryRepository.findAll()).hasSize(1);
    }


    @Test
    @DisplayName("Debe retornar 403 Forbidden al intentar crear sin rol ADMIN")
    @WithMockUser(roles = "USER")
    void shouldFailCreateWhenNotAdmin() throws Exception {
        CategoryRequest request = new CategoryRequest("Soccer");

        mockMvc.perform(post("/api/v1/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        assertThat(categoryRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Debe retornar 409 Conflict si el nombre ya existe")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnConflictOnDuplicateName() throws Exception {
        categoryRepository.save(Category.builder().nombre("Basketball").build());

        CategoryRequest request = new CategoryRequest("Basketball");

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Debe obtener lista paginada publicamente sin token")
    void shouldGetAllCategoriesPublicly() throws Exception {
        categoryRepository.save(Category.builder().nombre("Skate").status(CategoryStatus.ACTIVO).build());
        categoryRepository.save(Category.builder().nombre("Surf").status(CategoryStatus.ACTIVO).build());

        mockMvc.perform(get("/api/v1/categories?searchTerm=Ska"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Skate"));
    }

    @Test
    @DisplayName("Debe aplicar SOFT DELETE - Cambiar estado a INACTIVO")
    @WithMockUser(roles = "ADMIN")
    void shouldSoftDeleteCategory() throws  Exception {
        Category saved = categoryRepository.save(Category.builder().nombre("Tennis").build());

        mockMvc.perform(delete("/api/v1/categories/" + saved.getId()))
                .andExpect(status().isNoContent());

        Category inDb = categoryRepository.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getStatus()).isEqualTo(CategoryStatus.INACTIVO);
    }
}
