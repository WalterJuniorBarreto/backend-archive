package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.response.ProductResponse;
import com.ecommerce.api_geek_store.service.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private static final Logger log = LoggerFactory.getLogger(WishlistController.class);
    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> toggleWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {

        boolean added = wishlistService.toggleWishlist(userDetails.getUsername(), productId);

        String message = added ? "Producto agregado a favoritos" : "Producto eliminado de favoritos";

        log.info("Wishlist toggle: Usuario {} - Producto {} - Estado: {}",
                userDetails.getUsername(), productId, added ? "AGREGADO" : "ELIMINADO");

        return ResponseEntity.ok(Map.of(
                "message", message,
                "added", added
        ));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getMyWishlist(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(wishlistService.getMyWishlist(userDetails.getUsername()));
    }
}