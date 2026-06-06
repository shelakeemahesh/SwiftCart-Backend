package com.swiftcart.kafka.producer;

import com.swiftcart.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(
            java.util.Optional<KafkaTemplate<String, Object>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate.orElse(null);
    }

    public void publishOrderPlaced(String orderUuid) {
        if (kafkaTemplate == null) { log.warn("Kafka unavailable, skipping OrderPlaced event for {}", orderUuid); return; }
        log.info("Publishing OrderPlaced event to Kafka for UUID: {}", orderUuid);
        kafkaTemplate.send(KafkaConfig.ORDER_PLACED_TOPIC, orderUuid, orderUuid);
    }

    public void publishOrderStatusChange(String orderUuid, String status) {
        if (kafkaTemplate == null) { log.warn("Kafka unavailable, skipping OrderStatusChange event for {}", orderUuid); return; }
        log.info("Publishing OrderStatusChange event to Kafka for UUID: {} - status: {}", orderUuid, status);
        kafkaTemplate.send(KafkaConfig.ORDER_STATUS_TOPIC, orderUuid, status);
    }

    public void publishRatingRecalculation(Long productId) {
        if (kafkaTemplate == null) { log.warn("Kafka unavailable, skipping RatingRecalculation event for product {}", productId); return; }
        log.info("Publishing RatingRecalculation event to Kafka for Product ID: {}", productId);
        kafkaTemplate.send(KafkaConfig.PRODUCT_RATING_RECALC_TOPIC, String.valueOf(productId), productId);
    }

    public void publishOrderConfirmed(com.swiftcart.entity.Order order) {
        log.info("Publishing OrderConfirmed event to Kafka for UUID: {}", order.getOrderUuid());
        publishOrderStatusChange(order.getOrderUuid(), "CONFIRMED");
    }
}
