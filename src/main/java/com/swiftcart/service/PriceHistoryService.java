package com.swiftcart.service;

import com.swiftcart.entity.PriceDropAlert;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.ProductPriceHistory;
import com.swiftcart.entity.User;
import com.swiftcart.repository.PriceDropAlertRepository;
import com.swiftcart.repository.ProductPriceHistoryRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PriceHistoryService {

    private static final Logger log = LoggerFactory.getLogger(PriceHistoryService.class);

    private final ProductPriceHistoryRepository priceHistoryRepository;
    private final PriceDropAlertRepository alertRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public PriceHistoryService(
            ProductPriceHistoryRepository priceHistoryRepository,
            PriceDropAlertRepository alertRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.alertRepository = alertRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void recordPriceChange(Product product, BigDecimal newPrice) {
        List<ProductPriceHistory> history = priceHistoryRepository.findByProductIdOrderByRecordedAtAsc(product.getId());
        
        boolean priceChanged = true;
        if (!history.isEmpty()) {
            BigDecimal lastPrice = history.get(history.size() - 1).getPrice();
            priceChanged = lastPrice.compareTo(newPrice) != 0;
        }

        if (priceChanged) {
            ProductPriceHistory entry = new ProductPriceHistory(product, newPrice);
            priceHistoryRepository.save(entry);
            log.info("Recorded price change for product '{}': ₹{}", product.getName(), newPrice);

            // Check alerts
            List<PriceDropAlert> activeAlerts = alertRepository
                    .findByProductIdAndIsTriggeredFalseAndTargetPriceGreaterThanEqual(product.getId(), newPrice);

            for (PriceDropAlert alert : activeAlerts) {
                alert.setTriggered(true);
                alertRepository.save(alert);
                
                // Simulate sending email
                log.info("🔔 PRICE DROP ALERT TRIGGERED: Sending email to {} for product '{}' (Target: ₹{}, Current: ₹{})",
                        alert.getEmail(), product.getName(), alert.getTargetPrice(), newPrice);
                
                // Use existing notificationService to log/simulate if needed
                try {
                    notificationService.sendOrderConfirmation(alert.getEmail(), "Price Drop Alert: " + product.getName() + " is now ₹" + newPrice);
                } catch (Exception e) {
                    // Ignore email sender failure
                }
            }
        }
    }

    @Transactional
    public List<ProductPriceHistory> getOrCreatePriceHistory(String slug) {
        Product product = productRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductPriceHistory> history = priceHistoryRepository.findByProductIdOrderByRecordedAtAsc(product.getId());

        if (history.isEmpty()) {
            // Seed mock history of 5 points over the last 30 days fluctuating +/- 10%
            history = new ArrayList<>();
            Random rand = new Random();
            BigDecimal basePrice = product.getBasePrice();
            LocalDateTime now = LocalDateTime.now();

            for (int i = 4; i >= 0; i--) {
                double pct = 0.90 + (rand.nextDouble() * 0.20); // 90% to 110%
                BigDecimal mockPrice = basePrice.multiply(BigDecimal.valueOf(pct)).setScale(2, RoundingMode.HALF_UP);
                
                // Keep the final one exactly equal to current base price
                if (i == 0) {
                    mockPrice = basePrice;
                }

                ProductPriceHistory mockEntry = new ProductPriceHistory(product, mockPrice);
                mockEntry.setRecordedAt(now.minusDays(i * 7L));
                history.add(priceHistoryRepository.save(mockEntry));
            }
            log.info("Seeded mock price history for product '{}'", product.getName());
        }

        return history;
    }

    @Transactional
    public PriceDropAlert createAlert(Long userId, String slug, BigDecimal targetPrice, String email) {
        Product product = productRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        java.util.Optional<PriceDropAlert> existingAlertOpt = alertRepository
                .findByUserIdAndProductIdAndIsTriggeredFalse(userId, product.getId());

        if (existingAlertOpt.isPresent()) {
            PriceDropAlert alert = existingAlertOpt.get();
            alert.setTargetPrice(targetPrice);
            alert.setEmail(email);
            log.info("Updated existing price drop alert for user ID {} on product '{}' to target price ₹{}", userId, product.getName(), targetPrice);
            return alertRepository.save(alert);
        }

        PriceDropAlert alert = new PriceDropAlert(product, user, targetPrice, email);
        log.info("Created new price drop alert for user ID {} on product '{}' at target price ₹{}", userId, product.getName(), targetPrice);
        return alertRepository.save(alert);
    }
}
