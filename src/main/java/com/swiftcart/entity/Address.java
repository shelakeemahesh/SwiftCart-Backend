package com.swiftcart.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    private Label label;

    @Column(name = "recipient_name")
    private String recipientName;

    private String phone;

    private String pincode;

    @Column(name = "flat_house", columnDefinition = "TEXT")
    private String flatHouse;

    @Column(columnDefinition = "TEXT")
    private String area;

    private String city;

    private String state;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Default Constructor
    public Address() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Label getLabel() { return label; }
    public void setLabel(Label label) { this.label = label; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getFlatHouse() { return flatHouse; }
    public void setFlatHouse(String flatHouse) { this.flatHouse = flatHouse; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder
    public static AddressBuilder builder() {
        return new AddressBuilder();
    }

    public static class AddressBuilder {
        private User user;
        private Label label;
        private String recipientName;
        private String phone;
        private String pincode;
        private String flatHouse;
        private String area;
        private String city;
        private String state;
        private boolean isDefault = false;

        public AddressBuilder user(User user) {
            this.user = user;
            return this;
        }

        public AddressBuilder label(Label label) {
            this.label = label;
            return this;
        }

        public AddressBuilder recipientName(String recipientName) {
            this.recipientName = recipientName;
            return this;
        }

        public AddressBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public AddressBuilder pincode(String pincode) {
            this.pincode = pincode;
            return this;
        }

        public AddressBuilder flatHouse(String flatHouse) {
            this.flatHouse = flatHouse;
            return this;
        }

        public AddressBuilder area(String area) {
            this.area = area;
            return this;
        }

        public AddressBuilder city(String city) {
            this.city = city;
            return this;
        }

        public AddressBuilder state(String state) {
            this.state = state;
            return this;
        }

        public AddressBuilder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public Address build() {
            Address address = new Address();
            address.setUser(this.user);
            address.setLabel(this.label);
            address.setRecipientName(this.recipientName);
            address.setPhone(this.phone);
            address.setPincode(this.pincode);
            address.setFlatHouse(this.flatHouse);
            address.setArea(this.area);
            address.setCity(this.city);
            address.setState(this.state);
            address.setDefault(this.isDefault);
            return address;
        }
    }
}
