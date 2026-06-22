package com.ecommerce.api_geek_store.specification;

import com.ecommerce.api_geek_store.domain.model.enums.Genero;
import com.ecommerce.api_geek_store.domain.model.Product;
import com.ecommerce.api_geek_store.domain.model.enums.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withStatus(ProductStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Product> withSearchTerm(String searchTerm) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(searchTerm)) return null;
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            Predicate matchName = cb.like(cb.lower(root.get("nombre")), pattern);
            Predicate matchDesc = cb.like(cb.lower(root.get("descripcion")), pattern);
            return cb.or(matchName, matchDesc);
        };
    }

    public static Specification<Product> withPriceRange(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("precioBase"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("precioBase"), min);
            return cb.lessThanOrEqualTo(root.get("precioBase"), max);
        };
    }

    public static Specification<Product> withCategories(List<Long> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) return null;
            return root.get("category").get("id").in(categoryIds);
        };
    }

    public static Specification<Product> withBrands(List<Long> brandIds) {
        return (root, query, cb) -> {
            if (brandIds == null || brandIds.isEmpty()) return null;
            return root.get("brand").get("id").in(brandIds);
        };
    }

    public static Specification<Product> withGender(Genero gender) {
        return (root, query, cb) -> {
            if (gender == null) return null;
            return cb.equal(root.get("genero"), gender);
        };
    }

}
