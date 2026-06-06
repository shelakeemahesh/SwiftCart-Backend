package com.swiftcart.dto;

import com.swiftcart.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public class OrderRequest {

    @NotNull(message = "Address ID is required")
    private Long addressId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String couponCode;

    private String notes;

    public OrderRequest() {}

    public OrderRequest(Long addressId, PaymentMethod paymentMethod, String couponCode, String notes) {
        this.addressId = addressId;
        this.paymentMethod = paymentMethod;
        this.couponCode = couponCode;
        this.notes = notes;
    }

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
