package com.swiftcart.repository;

import com.swiftcart.entity.FlashSale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    @EntityGraph(attributePaths = {"product", "product.seller", "product.category", "product.images", "product.variants"})
    @Query("SELECT f FROM FlashSale f WHERE f.product.id = :productId AND f.startsAt <= :now AND f.endsAt >= :now")
    Optional<FlashSale> findActiveFlashSaleForProduct(@Param("productId") Long productId, @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"product", "product.seller", "product.category", "product.images", "product.variants"})
    @Query("SELECT f FROM FlashSale f WHERE f.startsAt <= :now AND f.endsAt >= :now")
    List<FlashSale> findActiveFlashSales(@Param("now") LocalDateTime now);
}
