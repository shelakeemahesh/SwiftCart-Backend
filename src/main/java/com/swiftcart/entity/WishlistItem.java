package com.swiftcart.entity;

import com.swiftcart.enums.*;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_items", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_product", columnNames = {"user_id", "product_id"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    public WishlistItem() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    // Builder
    public static WishlistItemBuilder builder() {
        return new WishlistItemBuilder();
    }

    public static class WishlistItemBuilder {
        private User user;
        private Product product;

        public WishlistItemBuilder user(User user) {
            this.user = user;
            return this;
        }

        public WishlistItemBuilder product(Product product) {
            this.product = product;
            return this;
        }

        public WishlistItem build() {
            WishlistItem item = new WishlistItem();
            item.setUser(this.user);
            item.setProduct(this.product);
            return item;
        }
    }
}
