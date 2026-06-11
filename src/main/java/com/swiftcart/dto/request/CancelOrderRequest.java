package com.swiftcart.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CancelOrderRequest {
    @NotBlank(message = "Order ID is required")
    private String orderId;

    public CancelOrderRequest() {}

    public CancelOrderRequest(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}
