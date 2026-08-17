package com.swiftcart.dto.response;

import java.math.BigDecimal;

public class ProductRecommendationDTO {
    private Long id;
    private String name;
    private String slug;
    private String brand;
    private String category;
    private BigDecimal price;
    private BigDecimal mrp;
    private BigDecimal averageRating;
    private int reviewCount;
    private boolean inStock;
    private String imageUrl;
    private String highlight;

    public ProductRecommendationDTO() {}

    public ProductRecommendationDTO(Long id, String name, String slug, String brand, String category,
                                  BigDecimal price, BigDecimal mrp, BigDecimal averageRating,
                                  int reviewCount, boolean inStock, String imageUrl, String highlight) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.mrp = mrp;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.inStock = inStock;
        this.imageUrl = imageUrl;
        this.highlight = highlight;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }

    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getHighlight() { return highlight; }
    public void setHighlight(String highlight) { this.highlight = highlight; }
}
