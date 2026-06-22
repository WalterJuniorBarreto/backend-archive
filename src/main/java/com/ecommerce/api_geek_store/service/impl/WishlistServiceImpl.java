package com.ecommerce.api_geek_store.service.impl;


import com.ecommerce.api_geek_store.api.dto.response.ProductResponse;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.domain.model.Product;
import com.ecommerce.api_geek_store.domain.model.User;
import com.ecommerce.api_geek_store.domain.model.WishlistItem;
import com.ecommerce.api_geek_store.domain.repository.ProductRepository;
import com.ecommerce.api_geek_store.domain.repository.UserRepository;
import com.ecommerce.api_geek_store.domain.repository.WishlistRepository;
import com.ecommerce.api_geek_store.service.WishlistService;
import com.ecommerce.api_geek_store.api.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {
    private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               UserRepository userRepository,
                               ProductRepository productRepository,
                               ProductMapper productMapper) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public boolean toggleWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));


        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado ID: " + productId));

        Optional<WishlistItem> itemOpt = wishlistRepository.findByUserIdAndProductId(user.getId(), productId);

        if (itemOpt.isPresent()) {
            wishlistRepository.delete(itemOpt.get());
            log.info("Producto ID {} eliminado del wishlist del usuario {}", productId, email);
            return false;
        } else {
            WishlistItem item = new WishlistItem(user, product);
            wishlistRepository.save(item);
            log.info("Producto ID {} agregado al wishlist del usuario {}", productId, email);
            return true;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getMyWishlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<WishlistItem> items = wishlistRepository.findByUserId(user.getId());

        return items.stream()
                .map(item -> productMapper.toResponse(item.getProduct()))
                .collect(Collectors.toList());
    }
}