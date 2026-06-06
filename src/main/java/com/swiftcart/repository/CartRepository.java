package com.swiftcart.repository;

import com.swiftcart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndProductIdAndVariantId(Long userId, Long productId, Long variantId);
    Optional<CartItem> findByUserIdAndProductIdAndVariantIsNull(Long userId, Long productId);
    void deleteByUserId(Long userId);
}
