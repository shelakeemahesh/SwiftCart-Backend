package com.swiftcart.dto;

import java.math.BigDecimal;

public class RazorpayOrderResponse {
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;

    public RazorpayOrderResponse() {}

    public RazorpayOrderResponse(String razorpayOrderId, BigDecimal amount, String currency, String keyId) {
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
    }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
}
