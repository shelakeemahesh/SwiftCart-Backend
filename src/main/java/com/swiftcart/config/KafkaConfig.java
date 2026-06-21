package com.swiftcart.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConfig {

    public static final String ORDER_PLACED_TOPIC = "order-placed";
    public static final String ORDER_STATUS_TOPIC = "order-status-events";
    public static final String PRODUCT_RATING_RECALC_TOPIC = "product-rating-recalc";
    public static final String PRODUCT_INDEXING_TOPIC = "product-indexing";

    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(ORDER_PLACED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderStatusTopic() {
        return TopicBuilder.name(ORDER_STATUS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productRatingRecalcTopic() {
        return TopicBuilder.name(PRODUCT_RATING_RECALC_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productIndexingTopic() {
        return TopicBuilder.name(PRODUCT_INDEXING_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
