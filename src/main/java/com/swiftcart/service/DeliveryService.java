package com.swiftcart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    // List of serviceable pincodes for simulation
    private final List<String> serviceablePincodes = List.of(
            "110001", "400001", "560001", "600001", "700001", "500001", "380001"
    );

    public boolean isPincodeServiceable(String pincode) {
        log.info("Checking pincode serviceability for: {}", pincode);
        if (pincode == null || pincode.isBlank()) {
            return false;
        }
        // For simulation, accept all 6-digit numeric strings as serviceable, or check our list
        return pincode.matches("\\d{6}");
    }

    public LocalDate calculateEstimatedDeliveryDate(String sellerCity, String buyerPincode) {
        log.info("Calculating delivery estimation from seller: {} to pincode: {}", sellerCity, buyerPincode);
        
        // Simple distance tier logic
        if (buyerPincode == null || buyerPincode.isEmpty()) {
            return LocalDate.now().plusDays(4); // default fallback
        }

        // Tiers based on starting digits of pincode
        char firstDigit = buyerPincode.charAt(0);
        if (sellerCity != null && sellerCity.equalsIgnoreCase("Bangalore")) {
            if (firstDigit == '5') {
                return LocalDate.now().plusDays(1); // Same state tier (Karnataka/South)
            } else if (firstDigit == '6' || firstDigit == '4') {
                return LocalDate.now().plusDays(2); // Neighboring state tier
            } else {
                return LocalDate.now().plusDays(4); // Rest of country tier
            }
        }
        return LocalDate.now().plusDays(3); // default tier
    }

    public List<Map<String, Object>> getShipmentTrackingEvents(String orderUuid) {
        log.info("Fetching shipment tracking history for order UUID: {}", orderUuid);
        
        return List.of(
                Map.of("status", "ORDER_PLACED", "timestamp", LocalDate.now().minusDays(2).toString(), "location", "Warehouse Central"),
                Map.of("status", "SHIPPED", "timestamp", LocalDate.now().minusDays(1).toString(), "location", "Hub Bangalore"),
                Map.of("status", "OUT_FOR_DELIVERY", "timestamp", LocalDate.now().toString(), "location", "Delivery Hub Local")
        );
    }
}
