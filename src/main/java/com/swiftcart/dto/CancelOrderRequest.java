package com.swiftcart.dto;

public class CancelOrderRequest {
    private String orderId;

    public CancelOrderRequest() {}

    public CancelOrderRequest(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}
