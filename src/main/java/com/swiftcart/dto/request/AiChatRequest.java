package com.swiftcart.dto.request;

public class AiChatRequest {
    private String message;
    private String intent;
    private Long productId;
    private String conversationId;

    public AiChatRequest() {}

    public AiChatRequest(String message) {
        this.message = message;
    }

    public AiChatRequest(String message, String intent, Long productId, String conversationId) {
        this.message = message;
        this.intent = intent;
        this.productId = productId;
        this.conversationId = conversationId;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}
