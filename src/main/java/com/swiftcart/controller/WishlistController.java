package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

import com.swiftcart.entity.Product;
import com.swiftcart.entity.User;
import com.swiftcart.entity.WishlistItem;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.repository.WishlistItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public WishlistController(WishlistItemRepository wishlistItemRepository,
                              ProductRepository productRepository,
                              UserRepository userRepository) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItem>>> getWishlist(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(wishlistItemRepository.findByUserId(user.getId())));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<WishlistItem>> addToWishlist(Principal principal, @PathVariable Long productId) {
        User user = getUserFromPrincipal(principal);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new RuntimeException("Product is already in your wishlist");
        }

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();

        return ResponseEntity.ok(ApiResponse.success(wishlistItemRepository.save(item)));
    }

    @DeleteMapping("/{productId}")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, String>>> removeFromWishlist(Principal principal, @PathVariable Long productId) {
        User user = getUserFromPrincipal(principal);

        if (!wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new RuntimeException("Product is not in your wishlist");
        }

        wishlistItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Product removed from wishlist")));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isInWishlist(Principal principal, @PathVariable Long productId) {
        User user = getUserFromPrincipal(principal);
        boolean exists = wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("inWishlist", exists)));
    }

    private User getUserFromPrincipal(Principal principal) {
        return com.swiftcart.security.SecurityUtil.getUserFromPrincipal(principal, userRepository);
    }
}
