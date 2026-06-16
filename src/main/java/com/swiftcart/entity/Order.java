package com.swiftcart.entity;

import com.swiftcart.enums.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_uuid", unique = true, nullable = false, length = 36)
    private String orderUuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_ref")
    private String paymentRef;

    @Column(name = "mrp_total", precision = 12, scale = 2)
    private BigDecimal mrpTotal;

    @Column(name = "discount_total", precision = 12, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "delivery_fee", precision = 8, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "platform_fee", precision = 8, scale = 2)
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "coupon_discount", precision = 10, scale = 2)
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "refund_id", length = 100)
    private String refundId;

    @Column(name = "placed_at", updatable = false)
    private LocalDateTime placedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.orderUuid == null) {
            this.orderUuid = UUID.randomUUID().toString();
        }
        placedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderUuid() { return orderUuid; }
    public void setOrderUuid(String orderUuid) { this.orderUuid = orderUuid; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentRef() { return paymentRef; }
    public void setPaymentRef(String paymentRef) { this.paymentRef = paymentRef; }

    public BigDecimal getMrpTotal() { return mrpTotal; }
    public void setMrpTotal(BigDecimal mrpTotal) { this.mrpTotal = mrpTotal; }

    public BigDecimal getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(BigDecimal discountTotal) { this.discountTotal = discountTotal; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getPlatformFee() { return platformFee; }
    public void setPlatformFee(BigDecimal platformFee) { this.platformFee = platformFee; }

    public BigDecimal getCouponDiscount() { return couponDiscount; }
    public void setCouponDiscount(BigDecimal couponDiscount) { this.couponDiscount = couponDiscount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public static class OrderBuilder {
        private User user;
        private Address address;
        private OrderStatus status = OrderStatus.PENDING;
        private PaymentStatus paymentStatus = PaymentStatus.PENDING;
        private PaymentMethod paymentMethod;
        private String paymentRef;
        private BigDecimal mrpTotal;
        private BigDecimal discountTotal = BigDecimal.ZERO;
        private BigDecimal deliveryFee = BigDecimal.ZERO;
        private BigDecimal platformFee = BigDecimal.ZERO;
        private BigDecimal couponDiscount = BigDecimal.ZERO;
        private BigDecimal finalAmount;
        private String couponCode;
        private String notes;
        private String razorpayOrderId;
        private String refundId;

        public OrderBuilder user(User user) {
            this.user = user;
            return this;
        }

        public OrderBuilder address(Address address) {
            this.address = address;
            return this;
        }

        public OrderBuilder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public OrderBuilder paymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
            return this;
        }

        public OrderBuilder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public OrderBuilder paymentRef(String paymentRef) {
            this.paymentRef = paymentRef;
            return this;
        }

        public OrderBuilder mrpTotal(BigDecimal mrpTotal) {
            this.mrpTotal = mrpTotal;
            return this;
        }

        public OrderBuilder discountTotal(BigDecimal discountTotal) {
            this.discountTotal = discountTotal;
            return this;
        }

        public OrderBuilder deliveryFee(BigDecimal deliveryFee) {
            this.deliveryFee = deliveryFee;
            return this;
        }

        public OrderBuilder platformFee(BigDecimal platformFee) {
            this.platformFee = platformFee;
            return this;
        }

        public OrderBuilder couponDiscount(BigDecimal couponDiscount) {
            this.couponDiscount = couponDiscount;
            return this;
        }

        public OrderBuilder finalAmount(BigDecimal finalAmount) {
            this.finalAmount = finalAmount;
            return this;
        }

        public OrderBuilder couponCode(String couponCode) {
            this.couponCode = couponCode;
            return this;
        }

        public OrderBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public OrderBuilder razorpayOrderId(String razorpayOrderId) {
            this.razorpayOrderId = razorpayOrderId;
            return this;
        }

        public OrderBuilder refundId(String refundId) {
            this.refundId = refundId;
            return this;
        }

        public Order build() {
            Order o = new Order();
            o.setUser(this.user);
            o.setAddress(this.address);
            o.setStatus(this.status);
            o.setPaymentStatus(this.paymentStatus);
            o.setPaymentMethod(this.paymentMethod);
            o.setPaymentRef(this.paymentRef);
            o.setMrpTotal(this.mrpTotal);
            o.setDiscountTotal(this.discountTotal);
            o.setDeliveryFee(this.deliveryFee);
            o.setPlatformFee(this.platformFee);
            o.setCouponDiscount(this.couponDiscount);
            o.setFinalAmount(this.finalAmount);
            o.setCouponCode(this.couponCode);
            o.setNotes(this.notes);
            o.setRazorpayOrderId(this.razorpayOrderId);
            o.setRefundId(this.refundId);
            return o;
        }
    }
}
