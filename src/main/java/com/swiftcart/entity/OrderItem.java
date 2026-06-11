package com.swiftcart.entity;

import com.swiftcart.enums.*;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product_snapshot", columnDefinition = "json")
    private ProductSnapshot productSnapshot;

    private int quantity;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    public OrderItem() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public ProductVariant getVariant() { return variant; }
    public void setVariant(ProductVariant variant) { this.variant = variant; }

    public ProductSnapshot getProductSnapshot() { return productSnapshot; }
    public void setProductSnapshot(ProductSnapshot productSnapshot) { this.productSnapshot = productSnapshot; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    // Nested Class
    public static class ProductSnapshot {
        private String name;
        private String brand;
        private String sku;
        private String imageUrl;
        private BigDecimal basePrice;
        private BigDecimal mrp;

        public ProductSnapshot() {}

        public ProductSnapshot(String name, String brand, String sku, String imageUrl, BigDecimal basePrice, BigDecimal mrp) {
            this.name = name;
            this.brand = brand;
            this.sku = sku;
            this.imageUrl = imageUrl;
            this.basePrice = basePrice;
            this.mrp = mrp;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

        public BigDecimal getMrp() { return mrp; }
        public void setMrp(BigDecimal mrp) { this.mrp = mrp; }

        // Builder for ProductSnapshot
        public static ProductSnapshotBuilder builder() {
            return new ProductSnapshotBuilder();
        }

        public static class ProductSnapshotBuilder {
            private String name;
            private String brand;
            private String sku;
            private String imageUrl;
            private BigDecimal basePrice;
            private BigDecimal mrp;

            public ProductSnapshotBuilder name(String name) {
                this.name = name;
                return this;
            }

            public ProductSnapshotBuilder brand(String brand) {
                this.brand = brand;
                return this;
            }

            public ProductSnapshotBuilder sku(String sku) {
                this.sku = sku;
                return this;
            }

            public ProductSnapshotBuilder imageUrl(String imageUrl) {
                this.imageUrl = imageUrl;
                return this;
            }

            public ProductSnapshotBuilder basePrice(BigDecimal basePrice) {
                this.basePrice = basePrice;
                return this;
            }

            public ProductSnapshotBuilder mrp(BigDecimal mrp) {
                this.mrp = mrp;
                return this;
            }

            public ProductSnapshot build() {
                return new ProductSnapshot(name, brand, sku, imageUrl, basePrice, mrp);
            }
        }
    }

    // Builder for OrderItem
    public static OrderItemBuilder builder() {
        return new OrderItemBuilder();
    }

    public static class OrderItemBuilder {
        private Order order;
        private Product product;
        private ProductVariant variant;
        private ProductSnapshot productSnapshot;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal mrp;
        private BigDecimal discount;
        private BigDecimal total;

        public OrderItemBuilder order(Order order) {
            this.order = order;
            return this;
        }

        public OrderItemBuilder product(Product product) {
            this.product = product;
            return this;
        }

        public OrderItemBuilder variant(ProductVariant variant) {
            this.variant = variant;
            return this;
        }

        public OrderItemBuilder productSnapshot(ProductSnapshot productSnapshot) {
            this.productSnapshot = productSnapshot;
            return this;
        }

        public OrderItemBuilder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderItemBuilder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public OrderItemBuilder mrp(BigDecimal mrp) {
            this.mrp = mrp;
            return this;
        }

        public OrderItemBuilder discount(BigDecimal discount) {
            this.discount = discount;
            return this;
        }

        public OrderItemBuilder total(BigDecimal total) {
            this.total = total;
            return this;
        }

        public OrderItem build() {
            OrderItem oi = new OrderItem();
            oi.setOrder(this.order);
            oi.setProduct(this.product);
            oi.setVariant(this.variant);
            oi.setProductSnapshot(this.productSnapshot);
            oi.setQuantity(this.quantity);
            oi.setUnitPrice(this.unitPrice);
            oi.setMrp(this.mrp);
            oi.setDiscount(this.discount);
            oi.setTotal(this.total);
            return oi;
        }
    }
}
