package com.swiftcart.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftcart.event.LiveActivityEvent;
import com.swiftcart.kafka.consumer.LiveActivityConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LiveActivityProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final LiveActivityConsumer liveActivityConsumer;

    @Value("${spring.kafka.enabled:false}")
    private boolean kafkaEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LiveActivityProducer(
            @Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate,
            LiveActivityConsumer liveActivityConsumer) {
        this.kafkaTemplate = kafkaTemplate;
        this.liveActivityConsumer = liveActivityConsumer;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LiveActivityProducer.class);

    public void publishEvent(LiveActivityEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            boolean sentToKafka = false;
            if (kafkaEnabled && kafkaTemplate != null) {
                try {
                    kafkaTemplate.send("swiftcart-live-activity", json);
                    sentToKafka = true;
                } catch (Exception ke) {
                    log.warn("Kafka publish failed for live activity event, falling back to in-memory: {}", ke.getMessage());
                }
            }
            if (!sentToKafka) {
                // In-memory fallback
                liveActivityConsumer.onEventReceived(json);
            }
        } catch (Exception e) {
            log.error("Failed to publish live activity event", e);
        }
    }
}
