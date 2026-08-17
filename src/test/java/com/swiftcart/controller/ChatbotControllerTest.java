package com.swiftcart.controller;

import com.swiftcart.dto.request.AiChatRequest;
import com.swiftcart.dto.request.ChatbotMessageRequest;
import com.swiftcart.dto.response.ActiveOrderDTO;
import com.swiftcart.dto.response.AiChatResponseDTO;
import com.swiftcart.dto.response.ApiResponse;
import com.swiftcart.dto.response.ChatbotResponseDTO;
import com.swiftcart.dto.response.ProductRecommendationDTO;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.service.OrderService;
import com.swiftcart.service.ai.AiChatbotService;
import com.swiftcart.service.ai.ProductVectorSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatbotControllerTest {

    @Mock
    private AiChatbotService aiChatbotService;

    @Mock
    private ProductVectorSyncService productVectorSyncService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    private ChatbotController chatbotController;

    @BeforeEach
    void setUp() {
        chatbotController = new ChatbotController(
                aiChatbotService,
                productVectorSyncService,
                orderService,
                orderRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("Should route natural language message to AI service")
    void testHandleMessageWithNaturalLanguage() {
        ChatbotMessageRequest request = new ChatbotMessageRequest();
        request.setMessage("Can you recommend wireless headphones?");

        ProductRecommendationDTO product = new ProductRecommendationDTO(
                1L, "SwiftSound Wireless", "swiftsound-wireless", "SwiftSound", "Audio",
                BigDecimal.valueOf(149.99), BigDecimal.valueOf(199.99), BigDecimal.valueOf(4.8), 20, true, "", "Top seller"
        );

        AiChatResponseDTO mockAiResp = AiChatResponseDTO.rag(
                "Here are the best wireless headphones in our catalog:",
                List.of(product),
                List.of("🚚 Track my order")
        );

        when(aiChatbotService.processMessage(any(), any(AiChatRequest.class))).thenReturn(mockAiResp);

        ResponseEntity<ApiResponse<ChatbotResponseDTO>> response = chatbotController.handleMessage(null, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("rag_recommendation", response.getBody().getData().getType());
        assertEquals(1, response.getBody().getData().getRecommendedProducts().size());
        assertEquals("SwiftSound Wireless", response.getBody().getData().getRecommendedProducts().get(0).getName());
    }

    @Test
    @DisplayName("Should answer dedicated /ask AI endpoint")
    void testAskAi() {
        AiChatRequest request = new AiChatRequest("Tell me about returns");

        AiChatResponseDTO mockAiResp = AiChatResponseDTO.text(
                "You can return items within 7 days of delivery.",
                List.of("🚚 Track my order"),
                "/info/returns-refunds"
        );

        when(aiChatbotService.processMessage(any(), eq(request))).thenReturn(mockAiResp);

        ResponseEntity<ApiResponse<AiChatResponseDTO>> response = chatbotController.askAi(null, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("You can return items within 7 days of delivery.", response.getBody().getData().getReply());
        assertEquals("/info/returns-refunds", response.getBody().getData().getActionUrl());
    }
}
