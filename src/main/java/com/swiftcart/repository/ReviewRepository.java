package com.swiftcart.repository;

import com.swiftcart.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByProductId(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByProductIdAndRating(Long productId, int rating, Pageable pageable);

    Optional<Review> findByUserIdAndProductIdAndOrderId(Long userId, Long productId, Long orderId);
    boolean existsByUserIdAndProductIdAndOrderId(Long userId, Long productId, Long orderId);

    @EntityGraph(attributePaths = {"user"})
    List<Review> findByProductId(Long productId);
}
