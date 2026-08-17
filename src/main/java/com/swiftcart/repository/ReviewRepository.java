package com.swiftcart.repository;

import com.swiftcart.entity.Review;
import com.swiftcart.enums.ReviewSentiment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByProductId(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByProductIdAndRating(Long productId, int rating, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByProductIdAndSentiment(Long productId, ReviewSentiment sentiment, Pageable pageable);

    Optional<Review> findByUserIdAndProductIdAndOrderId(Long userId, Long productId, Long orderId);
    boolean existsByUserIdAndProductIdAndOrderId(Long userId, Long productId, Long orderId);

    @EntityGraph(attributePaths = {"user"})
    List<Review> findByProductId(Long productId);

    @Query("SELECT r FROM Review r JOIN FETCH r.product p WHERE p.seller.id = :sellerId")
    List<Review> findByProductSellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT r FROM Review r WHERE r.sentiment IS NULL")
    List<Review> findBySentimentIsNull();

    long countBySentiment(ReviewSentiment sentiment);

    long countByProductIdAndSentiment(Long productId, ReviewSentiment sentiment);
}
