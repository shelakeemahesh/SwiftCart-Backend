package com.swiftcart.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ChatbotMessageRequest {
    @NotBlank(message = "Intent is required")
    private String intent;

    public ChatbotMessageRequest() {}

    public ChatbotMessageRequest(String intent) {
        this.intent = intent;
    }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
}
