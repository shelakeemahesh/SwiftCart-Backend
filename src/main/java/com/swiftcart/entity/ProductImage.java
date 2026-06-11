package com.swiftcart.entity;

import com.swiftcart.enums.*;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "product_images")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Column(name = "is_primary")
    private boolean isPrimary = false;

    @Column(name = "display_order")
    private int displayOrder = 0;

    public ProductImage() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    // Builder
    public static ProductImageBuilder builder() {
        return new ProductImageBuilder();
    }

    public static class ProductImageBuilder {
        private Product product;
        private String imageUrl;
        private boolean isPrimary = false;
        private int displayOrder = 0;

        public ProductImageBuilder product(Product product) {
            this.product = product;
            return this;
        }

        public ProductImageBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public ProductImageBuilder isPrimary(boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }

        public ProductImageBuilder displayOrder(int displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public ProductImage build() {
            ProductImage pi = new ProductImage();
            pi.setProduct(this.product);
            pi.setImageUrl(this.imageUrl);
            pi.setPrimary(this.isPrimary);
            pi.setDisplayOrder(this.displayOrder);
            return pi;
        }
    }
}
