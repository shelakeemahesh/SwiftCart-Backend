package com.swiftcart.entity;

import com.swiftcart.enums.*;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_product_order", columnNames = {"user_id", "product_id", "order_id"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @Column(nullable = false)
    private int rating;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> images = new ArrayList<>();

    @Column(name = "helpful_count")
    private int helpfulCount = 0;

    @Column(name = "is_verified_purchase")
    private boolean isVerifiedPurchase = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Review() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public int getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }

    public boolean isVerifiedPurchase() { return isVerifiedPurchase; }
    public void setVerifiedPurchase(boolean verifiedPurchase) { isVerifiedPurchase = verifiedPurchase; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder
    public static ReviewBuilder builder() {
        return new ReviewBuilder();
    }

    public static class ReviewBuilder {
        private Product product;
        private User user;
        private Order order;
        private int rating;
        private String title;
        private String body;
        private List<String> images = new ArrayList<>();
        private int helpfulCount = 0;
        private boolean isVerifiedPurchase = false;

        public ReviewBuilder product(Product product) {
            this.product = product;
            return this;
        }

        public ReviewBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ReviewBuilder order(Order order) {
            this.order = order;
            return this;
        }

        public ReviewBuilder rating(int rating) {
            this.rating = rating;
            return this;
        }

        public ReviewBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ReviewBuilder body(String body) {
            this.body = body;
            return this;
        }

        public ReviewBuilder images(List<String> images) {
            this.images = images;
            return this;
        }

        public ReviewBuilder helpfulCount(int helpfulCount) {
            this.helpfulCount = helpfulCount;
            return this;
        }

        public ReviewBuilder isVerifiedPurchase(boolean isVerifiedPurchase) {
            this.isVerifiedPurchase = isVerifiedPurchase;
            return this;
        }

        public Review build() {
            Review review = new Review();
            review.setProduct(this.product);
            review.setUser(this.user);
            review.setOrder(this.order);
            review.setRating(this.rating);
            review.setTitle(this.title);
            review.setBody(this.body);
            if (this.images != null) review.setImages(this.images);
            review.setHelpfulCount(this.helpfulCount);
            review.setVerifiedPurchase(this.isVerifiedPurchase);
            return review;
        }
    }
}
