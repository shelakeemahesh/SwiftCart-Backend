package com.swiftcart.controller;

import com.swiftcart.event.LiveActivityEvent;
import com.swiftcart.kafka.consumer.LiveActivityConsumer;
import com.swiftcart.kafka.producer.LiveActivityProducer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/activity")
public class LiveActivityController {

    private final LiveActivityConsumer liveActivityConsumer;
    private final LiveActivityProducer liveActivityProducer;

    public LiveActivityController(
            LiveActivityConsumer liveActivityConsumer,
            LiveActivityProducer liveActivityProducer) {
        this.liveActivityConsumer = liveActivityConsumer;
        this.liveActivityProducer = liveActivityProducer;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamActivityEvents() {
        SseEmitter emitter = new SseEmitter(1800000L); // 30 mins timeout
        liveActivityConsumer.addEmitter(emitter);
        return emitter;
    }

    @PostMapping("/event")
    public ResponseEntity<Void> triggerManualEvent(@RequestBody LiveActivityEvent event) {
        liveActivityProducer.publishEvent(event);
        return ResponseEntity.ok().build();
    }
}
