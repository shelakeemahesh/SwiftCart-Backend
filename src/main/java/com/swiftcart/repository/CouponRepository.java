package com.swiftcart.repository;

import com.swiftcart.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND (c.expiresAt IS NULL OR c.expiresAt > :now) AND (c.userId IS NULL OR c.userId = :userId)")
    List<Coupon> findAvailableCoupons(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
