package com.swiftcart.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderTrackingDTO {
    private String orderId;
    private String status;
    private List<TrackingItemDTO> items;
    private String deliveryAddress;
    private List<TimelineStepDTO> statusTimeline;
    private String estimatedDelivery;

    public OrderTrackingDTO() {}

    public OrderTrackingDTO(String orderId, String status, List<TrackingItemDTO> items, String deliveryAddress, List<TimelineStepDTO> statusTimeline, String estimatedDelivery) {
        this.orderId = orderId;
        this.status = status;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.statusTimeline = statusTimeline;
        this.estimatedDelivery = estimatedDelivery;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<TrackingItemDTO> getItems() { return items; }
    public void setItems(List<TrackingItemDTO> items) { this.items = items; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public List<TimelineStepDTO> getStatusTimeline() { return statusTimeline; }
    public void setStatusTimeline(List<TimelineStepDTO> statusTimeline) { this.statusTimeline = statusTimeline; }

    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    // --- Nested DTO Classes ---

    public static class TrackingItemDTO {
        private String name;
        private int qty;
        private BigDecimal price;
        private String imageUrl;

        public TrackingItemDTO() {}

        public TrackingItemDTO(String name, int qty, BigDecimal price, String imageUrl) {
            this.name = name;
            this.qty = qty;
            this.price = price;
            this.imageUrl = imageUrl;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class TimelineStepDTO {
        private String step;
        private String timestamp;
        private boolean completed;

        public TimelineStepDTO() {}

        public TimelineStepDTO(String step, String timestamp, boolean completed) {
            this.step = step;
            this.timestamp = timestamp;
            this.completed = completed;
        }

        public String getStep() { return step; }
        public void setStep(String step) { this.step = step; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
