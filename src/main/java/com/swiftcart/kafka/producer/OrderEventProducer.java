package com.swiftcart.kafka.producer;

import com.swiftcart.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final boolean kafkaEnabled;

    public OrderEventProducer(
            Optional<KafkaTemplate<String, Object>> kafkaTemplate,
            @Value("${spring.kafka.enabled:false}") boolean kafkaEnabled) {
        this.kafkaTemplate = kafkaTemplate.orElse(null);
        this.kafkaEnabled = kafkaEnabled;
    }

    public boolean isKafkaEnabled() {
        return kafkaEnabled && kafkaTemplate != null;
    }

    public void publishOrderPlaced(String orderUuid) {
        if (!isKafkaEnabled()) {
            log.warn("Kafka unavailable or disabled, skipping OrderPlaced event for {}", orderUuid);
            return;
        }
        log.info("Publishing OrderPlaced event to Kafka for UUID: {}", orderUuid);
        kafkaTemplate.send(KafkaConfig.ORDER_PLACED_TOPIC, orderUuid, orderUuid);
    }

    public void publishOrderStatusChange(String orderUuid, String status) {
        if (!isKafkaEnabled()) {
            log.warn("Kafka unavailable or disabled, skipping OrderStatusChange event for {}", orderUuid);
            return;
        }
        log.info("Publishing OrderStatusChange event to Kafka for UUID: {} - status: {}", orderUuid, status);
        kafkaTemplate.send(KafkaConfig.ORDER_STATUS_TOPIC, orderUuid, status);
    }

    public void publishRatingRecalculation(Long productId) {
        if (!isKafkaEnabled()) {
            log.warn("Kafka unavailable or disabled, skipping RatingRecalculation event for product {}", productId);
            return;
        }
        log.info("Publishing RatingRecalculation event to Kafka for Product ID: {}", productId);
        kafkaTemplate.send(KafkaConfig.PRODUCT_RATING_RECALC_TOPIC, String.valueOf(productId), String.valueOf(productId));
    }

    public void publishProductIndexing(Long productId) {
        if (!isKafkaEnabled()) {
            log.warn("Kafka unavailable or disabled, skipping ProductIndexing event for product {}", productId);
            return;
        }
        log.info("Publishing ProductIndexing event to Kafka for Product ID: {}", productId);
        kafkaTemplate.send(KafkaConfig.PRODUCT_INDEXING_TOPIC, String.valueOf(productId), String.valueOf(productId));
    }

    public void publishOrderConfirmed(com.swiftcart.entity.Order order) {
        log.info("Publishing OrderConfirmed event to Kafka for UUID: {}", order.getOrderUuid());
        publishOrderStatusChange(order.getOrderUuid(), "CONFIRMED");
    }
}
