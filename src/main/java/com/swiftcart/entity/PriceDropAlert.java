package com.swiftcart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "price_drop_alerts")
public class PriceDropAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal targetPrice;

    @Column(nullable = false)
    private String email;

    @Column(name = "is_triggered", nullable = false)
    private boolean isTriggered;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PriceDropAlert() {
        this.createdAt = LocalDateTime.now();
        this.isTriggered = false;
    }

    public PriceDropAlert(Product product, User user, BigDecimal targetPrice, String email) {
        this();
        this.product = product;
        this.user = user;
        this.targetPrice = targetPrice;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isTriggered() { return isTriggered; }
    public void setTriggered(boolean triggered) { isTriggered = triggered; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
