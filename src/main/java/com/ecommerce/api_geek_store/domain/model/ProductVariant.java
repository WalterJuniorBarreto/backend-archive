package com.ecommerce.api_geek_store.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes =  {
        @Index(name = "idx_variant_sku", columnList = "sku", unique = true),
        @Index(name = "idx_variant_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 30)
    private String color;

    @Column(nullable = false, length = 10)
    private Integer stock;

    @Column(name = "precio_adicional", precision = 10, scale = 2)
    private BigDecimal precioAdicional;

    @Version
    private Long version;

}