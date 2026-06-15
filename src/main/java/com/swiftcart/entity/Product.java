package com.swiftcart.entity;

import com.swiftcart.enums.*;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.BatchSize;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_category", columnList = "category_id"),
        @Index(name = "idx_seller", columnList = "seller_id"),
        @Index(name = "idx_brand", columnList = "brand")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"subCategories"})
    private Category category;

    private String brand;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(unique = true, nullable = false, length = 500)
    private String slug;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> highlights = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, String> specifications = new HashMap<>();

    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(name = "discount_percent", precision = 5, scale = 2, insertable = false, updatable = false)
    private BigDecimal discountPercent;

    @Column(name = "stock_qty")
    private int stockQty = 0;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_featured")
    private boolean isFeatured = false;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count")
    private int reviewCount = 0;

    @Column(name = "sold_count")
    private int soldCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<ProductVariant> variants = new LinkedHashSet<>();

    public Product() {}

    public BigDecimal getCalculatedDiscountPercent() {
        if (mrp == null || basePrice == null || mrp.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return mrp.subtract(basePrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(mrp, 2, java.math.RoundingMode.HALF_UP);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getHighlights() { return highlights; }
    public void setHighlights(List<String> highlights) { this.highlights = highlights; }

    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }

    public Set<ProductVariant> getVariants() { return variants; }
    public void setVariants(Set<ProductVariant> variants) { this.variants = variants; }

    // Builder
    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public static class ProductBuilder {
        private User seller;
        private Category category;
        private String brand;
        private String name;
        private String slug;
        private String description;
        private List<String> highlights = new ArrayList<>();
        private Map<String, String> specifications = new HashMap<>();
        private BigDecimal basePrice;
        private BigDecimal mrp;
        private int stockQty = 0;
        private boolean isActive = true;
        private boolean isFeatured = false;
        private BigDecimal averageRating = BigDecimal.ZERO;
        private int reviewCount = 0;
        private int soldCount = 0;

        public ProductBuilder seller(User seller) {
            this.seller = seller;
            return this;
        }

        public ProductBuilder category(Category category) {
            this.category = category;
            return this;
        }

        public ProductBuilder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public ProductBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProductBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public ProductBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ProductBuilder highlights(List<String> highlights) {
            this.highlights = highlights;
            return this;
        }

        public ProductBuilder specifications(Map<String, String> specifications) {
            this.specifications = specifications;
            return this;
        }

        public ProductBuilder basePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public ProductBuilder mrp(BigDecimal mrp) {
            this.mrp = mrp;
            return this;
        }

        public ProductBuilder stockQty(int stockQty) {
            this.stockQty = stockQty;
            return this;
        }

        public ProductBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public ProductBuilder isFeatured(boolean isFeatured) {
            this.isFeatured = isFeatured;
            return this;
        }

        public ProductBuilder averageRating(BigDecimal averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public ProductBuilder reviewCount(int reviewCount) {
            this.reviewCount = reviewCount;
            return this;
        }

        public ProductBuilder soldCount(int soldCount) {
            this.soldCount = soldCount;
            return this;
        }

        public Product build() {
            Product p = new Product();
            p.setSeller(this.seller);
            p.setCategory(this.category);
            p.setBrand(this.brand);
            p.setName(this.name);
            p.setSlug(this.slug);
            p.setDescription(this.description);
            if (this.highlights != null) p.setHighlights(this.highlights);
            if (this.specifications != null) p.setSpecifications(this.specifications);
            p.setBasePrice(this.basePrice);
            p.setMrp(this.mrp);
            p.setStockQty(this.stockQty);
            p.setActive(this.isActive);
            p.setFeatured(this.isFeatured);
            p.setAverageRating(this.averageRating);
            p.setReviewCount(this.reviewCount);
            p.setSoldCount(this.soldCount);
            return p;
        }
    }
}
