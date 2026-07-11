package com.swiftcart.repository;

import com.swiftcart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {
    @EntityGraph(attributePaths = {"product", "product.images", "product.seller", "product.category", "variant"})
    List<CartItem> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"product", "product.images", "product.seller", "product.category", "variant"})
    @org.springframework.data.jpa.repository.Query("SELECT c FROM CartItem c WHERE c.user.id = :userId")
    List<CartItem> findAndLockByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
    Optional<CartItem> findByUserIdAndProductIdAndVariantId(Long userId, Long productId, Long variantId);
    Optional<CartItem> findByUserIdAndProductIdAndVariantIsNull(Long userId, Long productId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM CartItem c WHERE c.user.id = :userId")
    void deleteByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
