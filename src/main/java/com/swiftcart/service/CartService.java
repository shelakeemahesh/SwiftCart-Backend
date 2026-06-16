package com.swiftcart.service;

import com.swiftcart.entity.CartItem;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.ProductVariant;
import com.swiftcart.entity.User;
import com.swiftcart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
    }

    public List<CartItem> getCartItems(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Transactional
    public CartItem addToCart(Long userId, Long productId, Long variantId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVariant variant = null;
        if (variantId != null) {
            variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Product variant not found"));
            if (variant.getProduct().getId() != product.getId()) {
                throw new RuntimeException("Variant does not belong to the selected product");
            }
        }

        int stockAvailable = (variant != null) ? variant.getStockQty() : product.getStockQty();
        if (stockAvailable < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + stockAvailable);
        }

        Optional<CartItem> existingItemOpt = (variant != null)
                ? cartRepository.findByUserIdAndProductIdAndVariantId(userId, productId, variantId)
                : cartRepository.findByUserIdAndProductIdAndVariantIsNull(userId, productId);

        CartItem item;
        if (existingItemOpt.isPresent()) {
            item = existingItemOpt.get();
            int newQuantity = item.getQuantity() + quantity;
            if (stockAvailable < newQuantity) {
                throw new RuntimeException("Insufficient stock for total updated quantity. Available: " + stockAvailable);
            }
            item.setQuantity(newQuantity);
        } else {
            item = CartItem.builder()
                    .user(user)
                    .product(product)
                    .variant(variant)
                    .quantity(quantity)
                    .build();
        }

        return cartRepository.save(item);
    }

    @Transactional
    public CartItem updateQuantity(Long userId, Long itemId, int quantity) {
        CartItem item = cartRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        int stockAvailable = (item.getVariant() != null)
                ? item.getVariant().getStockQty()
                : item.getProduct().getStockQty();

        if (stockAvailable < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + stockAvailable);
        }

        item.setQuantity(quantity);
        return cartRepository.save(item);
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        CartItem item = cartRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        cartRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    public void validateCart(Long userId) {
        List<CartItem> items = cartRepository.findByUserId(userId);
        if (items.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        for (CartItem item : items) {
            Product product = item.getProduct();
            if (!product.isActive()) {
                throw new RuntimeException("Product '" + product.getName() + "' is no longer active.");
            }

            int stockAvailable = (item.getVariant() != null)
                    ? item.getVariant().getStockQty()
                    : product.getStockQty();

            if (stockAvailable < item.getQuantity()) {
                throw new RuntimeException("Stock changed. Product '" + product.getName() + "' has insufficient stock.");
            }
        }
    }
}
