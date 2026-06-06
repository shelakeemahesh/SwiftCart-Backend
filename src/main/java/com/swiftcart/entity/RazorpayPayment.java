package com.swiftcart.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "razorpay_payments", indexes = {
        @Index(name = "idx_rzp_order", columnList = "razorpay_order_id"),
        @Index(name = "idx_rzp_payment", columnList = "razorpay_payment_id")
})
public class RazorpayPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiftcart_order_uuid", referencedColumnName = "order_uuid")
    private Order order;

    @Column(name = "razorpay_order_id", unique = true, length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    @Column(name = "refund_id", length = 100)
    private String refundId;

    @Column(name = "amount_paisa", nullable = false)
    private int amountPaisa;

    @Column(length = 10)
    private String currency = "INR";

    @Column(length = 50)
    private String method;

    @Enumerated(EnumType.STRING)
    private RazorpayPaymentStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "webhook_events", columnDefinition = "json")
    private String webhookEvents;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RazorpayPayment() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    public int getAmountPaisa() { return amountPaisa; }
    public void setAmountPaisa(int amountPaisa) { this.amountPaisa = amountPaisa; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public RazorpayPaymentStatus getStatus() { return status; }
    public void setStatus(RazorpayPaymentStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getWebhookEvents() { return webhookEvents; }
    public void setWebhookEvents(String webhookEvents) { this.webhookEvents = webhookEvents; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static RazorpayPaymentBuilder builder() {
        return new RazorpayPaymentBuilder();
    }

    public static class RazorpayPaymentBuilder {
        private Order order;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
        private String refundId;
        private int amountPaisa;
        private String currency = "INR";
        private String method;
        private RazorpayPaymentStatus status;
        private String failureReason;
        private String webhookEvents;

        public RazorpayPaymentBuilder order(Order order) {
            this.order = order;
            return this;
        }

        public RazorpayPaymentBuilder razorpayOrderId(String razorpayOrderId) {
            this.razorpayOrderId = razorpayOrderId;
            return this;
        }

        public RazorpayPaymentBuilder razorpayPaymentId(String razorpayPaymentId) {
            this.razorpayPaymentId = razorpayPaymentId;
            return this;
        }

        public RazorpayPaymentBuilder razorpaySignature(String razorpaySignature) {
            this.razorpaySignature = razorpaySignature;
            return this;
        }

        public RazorpayPaymentBuilder refundId(String refundId) {
            this.refundId = refundId;
            return this;
        }

        public RazorpayPaymentBuilder amountPaisa(int amountPaisa) {
            this.amountPaisa = amountPaisa;
            return this;
        }

        public RazorpayPaymentBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public RazorpayPaymentBuilder method(String method) {
            this.method = method;
            return this;
        }

        public RazorpayPaymentBuilder status(RazorpayPaymentStatus status) {
            this.status = status;
            return this;
        }

        public RazorpayPaymentBuilder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public RazorpayPaymentBuilder webhookEvents(String webhookEvents) {
            this.webhookEvents = webhookEvents;
            return this;
        }

        public RazorpayPayment build() {
            RazorpayPayment rp = new RazorpayPayment();
            rp.setOrder(this.order);
            rp.setRazorpayOrderId(this.razorpayOrderId);
            rp.setRazorpayPaymentId(this.razorpayPaymentId);
            rp.setRazorpaySignature(this.razorpaySignature);
            rp.setRefundId(this.refundId);
            rp.setAmountPaisa(this.amountPaisa);
            rp.setCurrency(this.currency);
            rp.setMethod(this.method);
            rp.setStatus(this.status);
            rp.setFailureReason(this.failureReason);
            rp.setWebhookEvents(this.webhookEvents);
            return rp;
        }
    }
}
