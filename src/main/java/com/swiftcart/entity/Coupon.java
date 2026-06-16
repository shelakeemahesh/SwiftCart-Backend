package com.swiftcart.entity;

import com.swiftcart.enums.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    @Column(precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount", precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private int usedCount = 0;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    public Coupon() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public CouponType getType() { return type; }
    public void setType(CouponType type) { this.type = type; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getMinOrderValue() { return minOrderValue; }
    public void setMinOrderValue(BigDecimal minOrderValue) { this.minOrderValue = minOrderValue; }

    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public static CouponBuilder builder() {
        return new CouponBuilder();
    }

    public static class CouponBuilder {
        private String code;
        private CouponType type;
        private BigDecimal value;
        private BigDecimal minOrderValue = BigDecimal.ZERO;
        private BigDecimal maxDiscount;
        private Integer usageLimit;
        private int usedCount = 0;
        private Long userId;
        private LocalDateTime expiresAt;
        private boolean isActive = true;

        public CouponBuilder code(String code) {
            this.code = code;
            return this;
        }

        public CouponBuilder type(CouponType type) {
            this.type = type;
            return this;
        }

        public CouponBuilder value(BigDecimal value) {
            this.value = value;
            return this;
        }

        public CouponBuilder minOrderValue(BigDecimal minOrderValue) {
            this.minOrderValue = minOrderValue;
            return this;
        }

        public CouponBuilder maxDiscount(BigDecimal maxDiscount) {
            this.maxDiscount = maxDiscount;
            return this;
        }

        public CouponBuilder usageLimit(Integer usageLimit) {
            this.usageLimit = usageLimit;
            return this;
        }

        public CouponBuilder usedCount(int usedCount) {
            this.usedCount = usedCount;
            return this;
        }

        public CouponBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public CouponBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public CouponBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Coupon build() {
            Coupon c = new Coupon();
            c.setCode(this.code);
            c.setType(this.type);
            c.setValue(this.value);
            c.setMinOrderValue(this.minOrderValue);
            c.setMaxDiscount(this.maxDiscount);
            c.setUsageLimit(this.usageLimit);
            c.setUsedCount(this.usedCount);
            c.setUserId(this.userId);
            c.setExpiresAt(this.expiresAt);
            c.setActive(this.isActive);
            return c;
        }
    }
}
