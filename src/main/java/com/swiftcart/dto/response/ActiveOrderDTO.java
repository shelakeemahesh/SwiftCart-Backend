package com.swiftcart.dto.response;

public class ActiveOrderDTO {
    private String orderId;
    private String status;
    private String productName;
    private String productThumbnailUrl;
    private String estimatedDelivery;
    private int totalItems;

    public ActiveOrderDTO() {}

    public ActiveOrderDTO(String orderId, String status, String productName, String productThumbnailUrl, String estimatedDelivery, int totalItems) {
        this.orderId = orderId;
        this.status = status;
        this.productName = productName;
        this.productThumbnailUrl = productThumbnailUrl;
        this.estimatedDelivery = estimatedDelivery;
        this.totalItems = totalItems;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductThumbnailUrl() { return productThumbnailUrl; }
    public void setProductThumbnailUrl(String productThumbnailUrl) { this.productThumbnailUrl = productThumbnailUrl; }

    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
}
