package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.request.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.response.ProductResponse;
import com.ecommerce.api_geek_store.api.mapper.ProductMapper;
import com.ecommerce.api_geek_store.domain.model.Brand;
import com.ecommerce.api_geek_store.domain.model.Category;
import com.ecommerce.api_geek_store.domain.model.Product;
import com.ecommerce.api_geek_store.domain.model.ProductVariant;
import com.ecommerce.api_geek_store.domain.model.enums.ProductStatus;
import com.ecommerce.api_geek_store.domain.repository.BrandRepository;
import com.ecommerce.api_geek_store.domain.repository.CategoryRepository;
import com.ecommerce.api_geek_store.domain.repository.ProductRepository;
import com.ecommerce.api_geek_store.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;


    @BeforeEach
    void setUp(){
        product = new Product();
        product.setId(1L);
        product.setNombre("Nike Air Max");
        product.setStatus(ProductStatus.DRAFT);

        productRequest = new ProductRequest(
                "Nike Air Max",
                "Descripcion genial",
                new BigDecimal("150.00"),
                1L,
                1L,
                "HOMBRE"
        );
    }


    @Test
    @DisplayName("Debe crear un producto exitosamente y asignarle estado DRAFT y slug")
    void crate_Success(){
        //preparar
        when(productRepository.existsByNombre(anyString())).thenReturn(false);
        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(product);
        when(categoryRepository.getReferenceById(anyLong())).thenReturn(new Category());
        when(brandRepository.getReferenceById(anyLong())).thenReturn(new Brand());
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(
                new ProductResponse(1L, "nike-air-max", "Nike Air Max", null, null, null, null, null, null, null, null,null,null,null)
        );

        //ejecutar
        ProductResponse response = productService.create(productRequest);

        //assert - verificar resultados
        assertThat(response).isNotNull();
        assertThat(response.nombre()).isEqualTo("Nike Air Max");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);

        //verificamos llamadas
        verify(productRepository, times(1)).save(product);

    }

    @Test
    @DisplayName("Debe lanzar DuplicateResourceException si el nombre ya existe")
    void create_ThrowsException_WhenNameExists(){
        //prepara
        when(productRepository.existsByNombre(productRequest.nombre())).thenReturn(true);

        //ejecutamos y verificamos resultados
        assertThrows(DuplicateResourceException.class, () -> {
            productService.create(productRequest);
        });

        //verificamos llamadas
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe publicar el producto si tiene estado DRAFT y tienee variantes")
    void publishProduct_Success(){
        //prepara
        ProductVariant variant = new ProductVariant(); //simulamos la variante
        product.setVariants(Set.of(variant));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        //ejecutamos
        productService.publishProduct(1L);

        //verificamos resultados
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        //verificamos llamadas
        verify(productRepository, times(1)).save(product);
    }


    @Test
    @DisplayName("Debe lanzar IllegalStateException si se intenta publica sin variantes")
    void publishProduct_ThrowsException_WhenNoVariants(){
        //prerara
        product.setVariants(Set.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        //ejecutamos y verificamos resultados
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            productService.publishProduct(1L);
        });

        assertThat(exception.getMessage()).contains("No se puede publicar un producto que no tiene tallas");

        //verificamos llamadas
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe abortar silenciosamente si el producto ya esta ACTIVE")
    void publishProduct_Aborts_WhenAlreadyActive(){
        product.setStatus(ProductStatus.ACTIVE);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.publishProduct(1L);

        verify(productRepository, never()).save(any(Product.class));
    }

}
