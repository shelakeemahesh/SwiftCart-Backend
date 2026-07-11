package com.swiftcart.kafka.consumer;

import com.swiftcart.config.KafkaConfig;
import com.swiftcart.entity.Order;
import com.swiftcart.enums.OrderStatus;
import com.swiftcart.entity.Review;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.repository.ReviewRepository;
import com.swiftcart.service.NotificationService;
import com.swiftcart.service.ProductService;
import com.swiftcart.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final ProductService productService;
    private final SearchService searchService;

    public OrderEventConsumer(
            OrderRepository orderRepository,
            ReviewRepository reviewRepository,
            NotificationService notificationService,
            ProductService productService,
            SearchService searchService) {
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.notificationService = notificationService;
        this.productService = productService;
        this.searchService = searchService;
    }

    @Transactional
    @KafkaListener(topics = KafkaConfig.ORDER_PLACED_TOPIC, groupId = "swiftcart-group")
    public void consumeOrderPlaced(String orderUuid) {
        log.info("Received OrderPlaced event from Kafka for UUID: {}", orderUuid);
        orderRepository.findByOrderUuid(orderUuid).ifPresent(order -> {
            String email = order.getUser().getEmail();
            if (email != null && !email.isBlank()) {
                notificationService.sendOrderConfirmation(email, orderUuid);
            }
        });
    }

    @Transactional
    @KafkaListener(topics = KafkaConfig.ORDER_STATUS_TOPIC, groupId = "swiftcart-group")
    public void consumeOrderStatusChange(
            @org.springframework.messaging.handler.annotation.Header(org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY) String orderUuid,
            @org.springframework.messaging.handler.annotation.Payload String status) {
        log.info("Received OrderStatusChange event from Kafka for UUID: {} - status: {}", orderUuid, status);
        orderRepository.findByOrderUuid(orderUuid).ifPresent(order -> {
            String email = order.getUser().getEmail();
            if (email != null && !email.isBlank()) {
                notificationService.sendOrderStatusUpdate(email, orderUuid, order.getStatus().name());
            }

            if (order.getStatus() == OrderStatus.CONFIRMED) {
                log.info("Order {} confirmed. Stock decrement is finalized.", orderUuid);
            }
        });
    }

    @KafkaListener(topics = KafkaConfig.PRODUCT_RATING_RECALC_TOPIC, groupId = "swiftcart-group")
    public void consumeProductRatingRecalculation(String productIdStr) {
        log.info("Received ProductRatingRecalc event from Kafka for Product ID: {}", productIdStr);
        try {
            Long productId = Long.parseLong(productIdStr);
            List<Review> reviews = reviewRepository.findByProductId(productId);
            if (reviews.isEmpty()) {
                productService.recalculateAverageRating(productId, BigDecimal.ZERO, 0);
                return;
            }

            double sum = 0.0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            double average = sum / reviews.size();
            BigDecimal averageRating = BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
            int count = reviews.size();

            productService.recalculateAverageRating(productId, averageRating, count);
        } catch (Exception e) {
            log.error("Failed to recalculate rating for product id {}: {}", productIdStr, e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaConfig.PRODUCT_INDEXING_TOPIC, groupId = "swiftcart-group")
    public void consumeProductIndexing(String productIdStr) {
        log.info("Received ProductIndexing event from Kafka for Product ID: {}", productIdStr);
        try {
            Long productId = Long.parseLong(productIdStr);
            searchService.indexProduct(productId);
        } catch (Exception e) {
            log.error("Failed to index product in ES for ID {}: {}", productIdStr, e.getMessage());
        }
    }
}
