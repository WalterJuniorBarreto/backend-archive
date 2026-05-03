package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.ProductResponse;

import java.util.List;


public interface WishlistService {

    boolean toggleWishlist(String email, Long productId);

    List<ProductResponse> getMyWishlist(String email);
}