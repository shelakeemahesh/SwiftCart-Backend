package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

import com.swiftcart.dto.request.CartItemRequest;
import com.swiftcart.entity.CartItem;
import com.swiftcart.entity.User;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItem>>> getCart(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(cartService.getCartItems(user.getId())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItem>> addToCart(Principal principal, @Valid @RequestBody CartItemRequest request) {
        User user = getUserFromPrincipal(principal);
        CartItem item = cartService.addToCart(user.getId(), request.getProductId(), request.getVariantId(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartItem>> updateQuantity(Principal principal, @PathVariable Long id, @RequestParam int quantity) {
        User user = getUserFromPrincipal(principal);
        CartItem item = cartService.updateQuantity(user.getId(), id, quantity);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> removeItem(Principal principal, @PathVariable Long id) {
        User user = getUserFromPrincipal(principal);
        cartService.removeItem(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Item removed from cart")));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> clearCart(Principal principal) {
        User user = getUserFromPrincipal(principal);
        cartService.clearCart(user.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Cart cleared successfully")));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, String>>> validateCart(Principal principal) {
        User user = getUserFromPrincipal(principal);
        cartService.validateCart(user.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Cart validated. Stock is available.")));
    }

    private User getUserFromPrincipal(Principal principal) {
        return com.swiftcart.security.SecurityUtil.getUserFromPrincipal(principal, userRepository);
    }
}
