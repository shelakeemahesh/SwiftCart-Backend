package com.swiftcart.service;

import com.swiftcart.entity.Coupon;
import com.swiftcart.enums.CouponType;
import com.swiftcart.entity.FlashSale;
import com.swiftcart.entity.Product;
import com.swiftcart.repository.CouponRepository;
import com.swiftcart.repository.FlashSaleRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PricingService {

    private final CouponRepository couponRepository;
    private final FlashSaleRepository flashSaleRepository;

    private PricingService self;

    @org.springframework.beans.factory.annotation.Autowired
    public void setSelf(@org.springframework.context.annotation.Lazy PricingService self) {
        this.self = self;
    }

    public PricingService(CouponRepository couponRepository, FlashSaleRepository flashSaleRepository) {
        this.couponRepository = couponRepository;
        this.flashSaleRepository = flashSaleRepository;
    }

    public BigDecimal getEffectiveProductPrice(Product product) {
        
        Optional<FlashSale> activeSale = self.getActiveFlashSale(product.getId());
        if (activeSale.isPresent()) {
            FlashSale sale = activeSale.get();
            if (sale.getSoldCount() < sale.getStockLimit()) {
                return sale.getSalePrice();
            }
        }
        return product.getBasePrice();
    }

    @Cacheable(value = "flashSales", key = "#productId")
    public Optional<FlashSale> getActiveFlashSale(Long productId) {
        return flashSaleRepository.findActiveFlashSaleForProduct(productId, LocalDateTime.now());
    }

    public Coupon validateCoupon(String code, BigDecimal orderValue, Long userId) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found or inactive"));

        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon has expired");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Coupon usage limit exceeded");
        }

        if (coupon.getUserId() != null && !coupon.getUserId().equals(userId)) {
            throw new RuntimeException("Coupon is not valid for this user");
        }

        if (orderValue.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new RuntimeException("Minimum order value to apply this coupon is " + coupon.getMinOrderValue());
        }

        return coupon;
    }

    public BigDecimal calculateCouponDiscount(Coupon coupon, BigDecimal orderValue) {
        BigDecimal discount = BigDecimal.ZERO;

        if (coupon.getType() == CouponType.PERCENT) {
            discount = orderValue.multiply(coupon.getValue().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP));
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else if (coupon.getType() == CouponType.FLAT) {
            discount = coupon.getValue();
            if (discount.compareTo(orderValue) > 0) {
                discount = orderValue;
            }
        } else if (coupon.getType() == CouponType.FREE_DELIVERY) {
            // Free delivery has zero additional discount
        }

        return discount;
    }

    private interface FlashSaleWithSoldInfo {
        default int getSoldValue() {
            return 0;
        }
    }
}
