package com.swiftcart.entity;

import com.swiftcart.enums.*;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(name = "variant_key", length = 100)
    private String variantKey;

    @Column(name = "variant_val")
    private String variantVal;

    @Column(name = "additional_price", precision = 10, scale = 2)
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    @Column(name = "stock_qty")
    private int stockQty = 0;

    @Column(unique = true, length = 100)
    private String sku;

    public ProductVariant() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getVariantKey() { return variantKey; }
    public void setVariantKey(String variantKey) { this.variantKey = variantKey; }

    public String getVariantVal() { return variantVal; }
    public void setVariantVal(String variantVal) { this.variantVal = variantVal; }

    public BigDecimal getAdditionalPrice() { return additionalPrice; }
    public void setAdditionalPrice(BigDecimal additionalPrice) { this.additionalPrice = additionalPrice; }

    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public static ProductVariantBuilder builder() {
        return new ProductVariantBuilder();
    }

    public static class ProductVariantBuilder {
        private Product product;
        private String variantKey;
        private String variantVal;
        private BigDecimal additionalPrice = BigDecimal.ZERO;
        private int stockQty = 0;
        private String sku;

        public ProductVariantBuilder product(Product product) {
            this.product = product;
            return this;
        }

        public ProductVariantBuilder variantKey(String variantKey) {
            this.variantKey = variantKey;
            return this;
        }

        public ProductVariantBuilder variantVal(String variantVal) {
            this.variantVal = variantVal;
            return this;
        }

        public ProductVariantBuilder additionalPrice(BigDecimal additionalPrice) {
            this.additionalPrice = additionalPrice;
            return this;
        }

        public ProductVariantBuilder stockQty(int stockQty) {
            this.stockQty = stockQty;
            return this;
        }

        public ProductVariantBuilder sku(String sku) {
            this.sku = sku;
            return this;
        }

        public ProductVariant build() {
            ProductVariant pv = new ProductVariant();
            pv.setProduct(this.product);
            pv.setVariantKey(this.variantKey);
            pv.setVariantVal(this.variantVal);
            pv.setAdditionalPrice(this.additionalPrice);
            pv.setStockQty(this.stockQty);
            pv.setSku(this.sku);
            return pv;
        }
    }
}
