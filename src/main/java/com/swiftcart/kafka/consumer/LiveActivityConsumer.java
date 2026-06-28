package com.swiftcart.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class LiveActivityConsumer {

    private final List<SseEmitter> emitters = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private final java.util.Random random = new java.util.Random();

    private final String[] mockNames = {"Sneha", "Rahul", "Priya", "Amit", "Vikram", "Anjali", "Rohan", "Karan", "Nisha", "Aditya"};
    private final String[] mockCities = {"Mumbai", "Bengaluru", "Delhi", "Pune", "Hyderabad", "Chennai", "Kolkata", "Ahmedabad", "Jaipur", "Surat"};
    private final String[] mockProducts = {
        "Apple iPhone 15 (128 GB) - Black",
        "Samsung Galaxy S24 Ultra",
        "Sony WH-1000XM5 Wireless Headphones",
        "Apple MacBook Air M3",
        "Nike Air Max Sneaker",
        "Dell XPS 13 Laptop",
        "iPad Pro 11-inch",
        "Nintendo Switch OLED"
    };

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        // Send initial connection event to prevent buffering/timeouts
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("established"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        List<SseEmitter> deadEmitters = new java.util.concurrent.CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("ping"));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 12000)
    public void generateMockActivity() {
        if (emitters.isEmpty()) {
            return;
        }
        String type = random.nextBoolean() ? "VIEW" : "PURCHASE";
        String name = mockNames[random.nextInt(mockNames.length)];
        String city = mockCities[random.nextInt(mockCities.length)];
        String product = mockProducts[random.nextInt(mockProducts.length)];

        try {
            com.swiftcart.event.LiveActivityEvent event = new com.swiftcart.event.LiveActivityEvent(type, name, product, city);
            String json = objectMapper.writeValueAsString(event);
            onEventReceived(json);
        } catch (Exception e) {
            // Ignore
        }
    }

    @KafkaListener(
        topics = "swiftcart-live-activity",
        groupId = "swiftcart-live-activity-group",
        autoStartup = "${spring.kafka.enabled:false}"
    )
    public void consumeActivity(String message) {
        onEventReceived(message);
    }

    // Direct receiver for local in-memory fallback + Kafka listener
    public void onEventReceived(String jsonEvent) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("activity-feed")
                        .data(jsonEvent));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }
}
