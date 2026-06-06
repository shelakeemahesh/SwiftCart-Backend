package com.swiftcart.repository;

import com.swiftcart.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.variants LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.category WHERE p.slug = :slug AND p.isActive = true")
    Optional<Product> findBySlugWithDetails(@Param("slug") String slug);
 
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.variants LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findAndLockById(@Param("id") Long id);

    boolean existsBySlug(String slug);

    List<Product> findTop20ByIsActiveTrueOrderBySoldCountDesc();

    List<Product> findTop20ByIsActiveTrueOrderByCreatedAtDesc();

    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id <> :productId AND p.isActive = true")
    List<Product> findRelatedProducts(@Param("categoryId") Long categoryId, @Param("productId") Long productId, Pageable pageable);

    List<Product> findBySellerId(Long sellerId, Pageable pageable);
}
