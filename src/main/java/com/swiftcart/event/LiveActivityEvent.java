package com.swiftcart.event;

import java.io.Serializable;

public class LiveActivityEvent implements Serializable {
    private String type; // VIEW, PURCHASE, ADD_TO_CART, STOCK_ALERT
    private String username;
    private String productName;
    private String city;
    private Long timestamp;

    public LiveActivityEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public LiveActivityEvent(String type, String username, String productName, String city) {
        this();
        this.type = type;
        this.username = username;
        this.productName = productName;
        this.city = city;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
