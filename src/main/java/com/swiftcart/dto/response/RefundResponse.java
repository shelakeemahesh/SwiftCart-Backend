package com.swiftcart.dto.response;

public class RefundResponse {
    private String refundId;
    private String status;

    public RefundResponse() {}

    public RefundResponse(String refundId, String status) {
        this.refundId = refundId;
        this.status = status;
    }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
