package com.swiftcart.dto.response;

import java.util.ArrayList;
import java.util.List;

public class ChatbotResponseDTO {
    private String messageText;
    private String type; // "rag_recommendation", "order_card", "options", "text", "policy"
    private ActiveOrderDTO order;
    private List<String> options = new ArrayList<>();
    private String actionUrl;
    private List<ProductRecommendationDTO> recommendedProducts = new ArrayList<>();

    public ChatbotResponseDTO() {}

    public ChatbotResponseDTO(String messageText, String type, ActiveOrderDTO order, List<String> options, String actionUrl) {
        this.messageText = messageText;
        this.type = type;
        this.order = order;
        this.options = options != null ? options : new ArrayList<>();
        this.actionUrl = actionUrl;
        this.recommendedProducts = new ArrayList<>();
    }

    public ChatbotResponseDTO(String messageText, String type, ActiveOrderDTO order, List<String> options, String actionUrl, List<ProductRecommendationDTO> recommendedProducts) {
        this.messageText = messageText;
        this.type = type;
        this.order = order;
        this.options = options != null ? options : new ArrayList<>();
        this.actionUrl = actionUrl;
        this.recommendedProducts = recommendedProducts != null ? recommendedProducts : new ArrayList<>();
    }

    public static ChatbotResponseDTO fromAiResponse(AiChatResponseDTO ai) {
        return new ChatbotResponseDTO(
                ai.getReply(),
                ai.getType(),
                ai.getOrder(),
                ai.getOptions(),
                ai.getActionUrl(),
                ai.getRecommendedProducts()
        );
    }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public ActiveOrderDTO getOrder() { return order; }
    public void setOrder(ActiveOrderDTO order) { this.order = order; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public List<ProductRecommendationDTO> getRecommendedProducts() { return recommendedProducts; }
    public void setRecommendedProducts(List<ProductRecommendationDTO> recommendedProducts) { this.recommendedProducts = recommendedProducts; }
}
