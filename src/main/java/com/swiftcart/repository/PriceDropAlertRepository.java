package com.swiftcart.repository;

import com.swiftcart.entity.PriceDropAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PriceDropAlertRepository extends JpaRepository<PriceDropAlert, Long> {
    List<PriceDropAlert> findByProductIdAndIsTriggeredFalseAndTargetPriceGreaterThanEqual(Long productId, BigDecimal price);
    java.util.Optional<PriceDropAlert> findByUserIdAndProductIdAndIsTriggeredFalse(Long userId, Long productId);
}
