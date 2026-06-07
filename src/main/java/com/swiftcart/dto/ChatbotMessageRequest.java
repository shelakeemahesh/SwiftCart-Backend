package com.swiftcart.dto;

public class ChatbotMessageRequest {
    private String intent;

    public ChatbotMessageRequest() {}

    public ChatbotMessageRequest(String intent) {
        this.intent = intent;
    }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
}
