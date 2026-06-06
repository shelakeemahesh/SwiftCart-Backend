package com.swiftcart.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentVerifyRequest {

    @NotBlank(message = "razorpayPaymentId is required")
    private String razorpayPaymentId;

    @NotBlank(message = "razorpayOrderId is required")
    private String razorpayOrderId;

    @NotBlank(message = "razorpaySignature is required")
    private String razorpaySignature;

    @NotBlank(message = "swiftcartOrderUuid is required")
    private String swiftcartOrderUuid;

    public PaymentVerifyRequest() {}

    public PaymentVerifyRequest(String razorpayPaymentId, String razorpayOrderId, String razorpaySignature, String swiftcartOrderUuid) {
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpaySignature = razorpaySignature;
        this.swiftcartOrderUuid = swiftcartOrderUuid;
    }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }

    public String getSwiftcartOrderUuid() { return swiftcartOrderUuid; }
    public void setSwiftcartOrderUuid(String swiftcartOrderUuid) { this.swiftcartOrderUuid = swiftcartOrderUuid; }
}
