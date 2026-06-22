package com.ecommerce.api_geek_store.integration;

import com.ecommerce.api_geek_store.api.dto.request.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.request.ProductSearchCriteria;
import com.ecommerce.api_geek_store.api.dto.response.ProductResponse;
import com.ecommerce.api_geek_store.domain.model.Brand;
import com.ecommerce.api_geek_store.domain.model.Category;
import com.ecommerce.api_geek_store.domain.model.Product;
import com.ecommerce.api_geek_store.domain.model.enums.ProductStatus;
import com.ecommerce.api_geek_store.domain.repository.BrandRepository;
import com.ecommerce.api_geek_store.domain.repository.CategoryRepository;
import com.ecommerce.api_geek_store.domain.repository.ProductRepository;
import com.ecommerce.api_geek_store.service.ProductService;
import com.ecommerce.api_geek_store.service.notification.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "mail.from.address=test-user@archive.com",
        "MAIL_USERNAME=test-user@archive.com",
        "application.frontend.url=http://localhost:3000"
})
// 2. EL PERFIL: Le dice a Spring que lea el application-test.properties
@ActiveProfiles("test")
@Testcontainers
@Transactional
class ProductIntegrationTest{

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("ecommerce_test")
            .withUsername("test")
            .withPassword("test");


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", ()  -> "create-drop");
    }


    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    private Category category;
    private Brand brand;

    @BeforeEach
    void setUp(){
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();

        category = new Category();
        category.setNombre("Sneakers");
        categoryRepository.save(category);

        brand = new Brand();
        brand.setNombre("Nike");
        brandRepository.save(brand);

        Product p1 = new Product();
        p1.setNombre("Nike Air Max");
        p1.setSlug("nike-air-max");
        p1.setDescripcion("Zapatillas deportivas de alto rendimiento");
        p1.setPrecioBase(new BigDecimal("150.00"));
        p1.setStatus(ProductStatus.ACTIVE);
        p1.setCategory(category);
        p1.setBrand(brand);

        Product p2 = new Product();
        p2.setNombre("Nike Dunk Low");
        p2.setSlug("nike-dunk-low");
        p2.setDescripcion("Zapatillas urbanas clásicas");
        p2.setPrecioBase(new BigDecimal("110.00"));
        p2.setStatus(ProductStatus.DRAFT);
        p2.setCategory(category);
        p2.setBrand(brand);

        productRepository.saveAll(List.of(p1, p2));
    }


    @Test
    @DisplayName("Debe persistir en el contenedor Docker de Postgres y retornar el record inmutable")
    void createProduct_Integration_Success(){
        ProductRequest request = new ProductRequest(
                "Adidas Samba",
                "Clasica de clasicas",
                new BigDecimal("120.00"),
                category.getId(),
                brand.getId(),
                "UNISEX"
        );

        ProductResponse response = productService.create(request);


        assertThat(response).isNotNull();
        assertThat(response.id()).isGreaterThan(0L);
        assertThat(response.slug()).isEqualTo("adidas-samba");

        Product inDb = productRepository.findById(response.id()).orElseThrow();
        assertThat(inDb.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }


    @Test
    @DisplayName("Specification Testing: Debe filtrar productos ACTIVE correctamente en postgres real")
    void dinfAllWithFilters_Integration_Success(){
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Air", ProductStatus.ACTIVE, null, null, null, null, null, null
        );

        PageRequest pageable = PageRequest.of(0, 10);

        Page<ProductResponse> result = productService.findAllWithFilters(criteria, pageable);

        assertThat(result.getContent()).hasSize(1);
        ProductResponse found = result.getContent().get(0);
        assertThat(found.nombre()).isEqualTo("Nike Air Max");
        assertThat(found.brand().nombre()).isEqualTo("Nike");
    }
}
