package com.swiftcart.dto.response;

import java.util.List;

public class ChatbotResponseDTO {
    private String messageText;
    private String type;            // "text" | "order_card" | "options"
    private ActiveOrderDTO order;   // populated if type = order_card
    private List<String> options;   // next quick-reply suggestions
    private String actionUrl;       // optional redirect link

    public ChatbotResponseDTO() {}

    public ChatbotResponseDTO(String messageText, String type, ActiveOrderDTO order, List<String> options, String actionUrl) {
        this.messageText = messageText;
        this.type = type;
        this.order = order;
        this.options = options;
        this.actionUrl = actionUrl;
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
}
