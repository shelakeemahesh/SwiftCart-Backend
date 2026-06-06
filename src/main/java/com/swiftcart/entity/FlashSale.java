package com.swiftcart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "flash_sales")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sale_price", precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "stock_limit")
    private Integer stockLimit;

    @Column(name = "sold_count")
    private int soldCount = 0;

    public FlashSale() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }

    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }

    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }

    public Integer getStockLimit() { return stockLimit; }
    public void setStockLimit(Integer stockLimit) { this.stockLimit = stockLimit; }

    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }

    // Builder
    public static FlashSaleBuilder builder() {
        return new FlashSaleBuilder();
    }

    public static class FlashSaleBuilder {
        private Product product;
        private BigDecimal salePrice;
        private LocalDateTime startsAt;
        private LocalDateTime endsAt;
        private Integer stockLimit;
        private int soldCount = 0;

        public FlashSaleBuilder product(Product product) {
            this.product = product;
            return this;
        }

        public FlashSaleBuilder salePrice(BigDecimal salePrice) {
            this.salePrice = salePrice;
            return this;
        }

        public FlashSaleBuilder startsAt(LocalDateTime startsAt) {
            this.startsAt = startsAt;
            return this;
        }

        public FlashSaleBuilder endsAt(LocalDateTime endsAt) {
            this.endsAt = endsAt;
            return this;
        }

        public FlashSaleBuilder stockLimit(Integer stockLimit) {
            this.stockLimit = stockLimit;
            return this;
        }

        public FlashSaleBuilder soldCount(int soldCount) {
            this.soldCount = soldCount;
            return this;
        }

        public FlashSale build() {
            FlashSale fs = new FlashSale();
            fs.setProduct(this.product);
            fs.setSalePrice(this.salePrice);
            fs.setStartsAt(this.startsAt);
            fs.setEndsAt(this.endsAt);
            fs.setStockLimit(this.stockLimit);
            fs.setSoldCount(this.soldCount);
            return fs;
        }
    }
}
