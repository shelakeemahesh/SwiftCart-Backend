package com.swiftcart.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RefundOrderRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    private String reason;

    public RefundOrderRequest() {}

    public RefundOrderRequest(String orderId) {
        this.orderId = orderId;
    }

    public RefundOrderRequest(String orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
