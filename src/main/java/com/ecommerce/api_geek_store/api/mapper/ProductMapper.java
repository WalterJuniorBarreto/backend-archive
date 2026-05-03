package com.ecommerce.api_geek_store.api.mapper;

import com.ecommerce.api_geek_store.api.dto.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.ProductResponse;
import com.ecommerce.api_geek_store.domain.model.Product;
import com.ecommerce.api_geek_store.domain.model.ProductImage;
import com.ecommerce.api_geek_store.domain.model.ProductVariant;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Component
public class ProductMapper {



    public Product toEntity(ProductRequest request) {
        if (request == null) return null;

        Product product = new Product();
        product.setNombre(request.nombre());
        product.setDescripcion(request.descripcion());
        product.setPrecio(request.precio());
        product.setDescuento(request.descuento() != null ? request.descuento() : 0);
        product.setGenero(request.genero());
        product.setFeatured(Boolean.TRUE.equals(request.featured()));

        if (request.images() != null) {
            request.images().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .forEach(url -> product.addImage(new ProductImage(url)));
        }

        if (request.variantes() != null) {
            for (ProductRequest.VariantRequest vReq : request.variantes()) {
                ProductVariant variant = new ProductVariant();
                variant.setColor(vReq.color() != null ? vReq.color().toUpperCase() : "S/C");
                variant.setColorHex(vReq.colorHex());
                variant.setTalla(vReq.talla() != null ? vReq.talla().toUpperCase() : "S/T");
                variant.setStock(vReq.stock() != null ? vReq.stock() : 0);
                product.addVariant(variant);
            }
        }
        return product;
    }


    public ProductResponse toResponse(Product product) {
        if (product == null) return null;

        BigDecimal precioFinal = calcularPrecioFinal(product.getPrecio(), product.getDescuento());

        List<ProductResponse.VariantResponse> variantResponses = (product.getVariants() != null)
                ? product.getVariants().stream()
                .map(v -> new ProductResponse.VariantResponse(v.getId(), v.getColor(), v.getColorHex(), v.getTalla(), v.getStock()))
                .toList() : Collections.emptyList();

        List<String> imageUrls = (product.getImages() != null)
                ? product.getImages().stream().map(ProductImage::getUrl).toList() : Collections.emptyList();

        return new ProductResponse(
                product.getId(),           // 1
                product.getNombre(),        // 2
                product.getDescripcion(),   // 3
                product.getPrecio(),        // 4
                product.getDescuento(),     // 5
                precioFinal,                // 6
                product.getImagenUrl(),     // 7
                imageUrls,                  // 8
                (product.getCategory() != null) ? product.getCategory().getId() : null,   // 9
                (product.getCategory() != null) ? product.getCategory().getNombre() : "Sin Categoría", // 10
                (product.getBrand() != null) ? product.getBrand().getId() : null,        // 11
                (product.getBrand() != null) ? product.getBrand().getNombre() : "Sin Marca", // 12
                (product.getGenero() != null) ? product.getGenero().name() : null,       // 13
                product.getFeatured(),      // 14
                variantResponses,           // 15
                product.getTotalStock()     // 16
        );
    }

    private BigDecimal calcularPrecioFinal(BigDecimal precio, Integer descuento) {
        if (descuento == null || descuento <= 0) return precio;

        BigDecimal descuentoMonto = precio
                .multiply(BigDecimal.valueOf(descuento))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return precio.subtract(descuentoMonto);
    }
}