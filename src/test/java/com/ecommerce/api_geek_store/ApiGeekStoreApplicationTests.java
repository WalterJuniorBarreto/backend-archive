package com.ecommerce.api_geek_store;

import com.ecommerce.api_geek_store.service.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "mail.from.address=test-user@archive.com",
        "MAIL_USERNAME=test-user@archive.com",
        "application.frontend.url=http://localhost:3000" // Añade esta también por si acaso
})
@ActiveProfiles("test")
@Testcontainers
class ApiGeekStoreApplicationTests {


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

	@Test
	@DisplayName("Prueba de Humo: Validar que el Contexto de Spring Boot levante correctamente")
	void contextLoads() {
	}

}