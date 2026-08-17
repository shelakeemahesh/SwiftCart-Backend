package com.swiftcart.dto.response;

import java.util.List;

public class AiChatResponseDTO {
    private String reply;
    private String type; // "rag_recommendation", "order_card", "policy", "text", "options"
    private List<ProductRecommendationDTO> recommendedProducts;
    private ActiveOrderDTO order;
    private List<String> options;
    private String actionUrl;

    public AiChatResponseDTO() {}

    public AiChatResponseDTO(String reply, String type, List<ProductRecommendationDTO> recommendedProducts,
                             ActiveOrderDTO order, List<String> options, String actionUrl) {
        this.reply = reply;
        this.type = type;
        this.recommendedProducts = recommendedProducts;
        this.order = order;
        this.options = options;
        this.actionUrl = actionUrl;
    }

    public static AiChatResponseDTO text(String reply, List<String> options, String actionUrl) {
        return new AiChatResponseDTO(reply, "text", List.of(), null, options, actionUrl);
    }

    public static AiChatResponseDTO rag(String reply, List<ProductRecommendationDTO> products, List<String> options) {
        return new AiChatResponseDTO(reply, "rag_recommendation", products, null, options, null);
    }

    public static AiChatResponseDTO order(String reply, ActiveOrderDTO order, List<String> options) {
        return new AiChatResponseDTO(reply, "order_card", List.of(), order, options, null);
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<ProductRecommendationDTO> getRecommendedProducts() { return recommendedProducts; }
    public void setRecommendedProducts(List<ProductRecommendationDTO> recommendedProducts) { this.recommendedProducts = recommendedProducts; }

    public ActiveOrderDTO getOrder() { return order; }
    public void setOrder(ActiveOrderDTO order) { this.order = order; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
}
