package com.swiftcart.controller;

import com.swiftcart.dto.request.AiChatRequest;
import com.swiftcart.dto.request.CancelOrderRequest;
import com.swiftcart.dto.request.ChatbotMessageRequest;
import com.swiftcart.dto.response.ApiResponse;
import com.swiftcart.dto.response.AiChatResponseDTO;
import com.swiftcart.dto.response.ChatbotResponseDTO;
import com.swiftcart.dto.response.ReturnPolicyDTO;
import com.swiftcart.entity.Order;
import com.swiftcart.entity.User;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.service.OrderService;
import com.swiftcart.service.ai.AiChatbotService;
import com.swiftcart.service.ai.ProductVectorSyncService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatbotController {

    private final AiChatbotService aiChatbotService;
    private final ProductVectorSyncService productVectorSyncService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ChatbotController(
            AiChatbotService aiChatbotService,
            ProductVectorSyncService productVectorSyncService,
            OrderService orderService,
            OrderRepository orderRepository,
            UserRepository userRepository) {
        this.aiChatbotService = aiChatbotService;
        this.productVectorSyncService = productVectorSyncService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /**
     * Unified chat endpoint supporting structured intent buttons and natural language questions.
     */
    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatbotResponseDTO>> handleMessage(
            Principal principal,
            @RequestBody ChatbotMessageRequest request) {

        User user = principal != null ? getUserFromPrincipal(principal) : null;
        AiChatRequest aiRequest = new AiChatRequest(
                request.getMessage(),
                request.getIntent(),
                request.getProductId(),
                request.getConversationId()
        );

        AiChatResponseDTO aiResponse = aiChatbotService.processMessage(user, aiRequest);
        ChatbotResponseDTO response = ChatbotResponseDTO.fromAiResponse(aiResponse);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Dedicated RAG AI Chat assistant endpoint.
     */
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<AiChatResponseDTO>> askAi(
            Principal principal,
            @RequestBody AiChatRequest request) {

        User user = principal != null ? getUserFromPrincipal(principal) : null;
        AiChatResponseDTO aiResponse = aiChatbotService.processMessage(user, request);

        return ResponseEntity.ok(ApiResponse.success(aiResponse));
    }

    /**
     * Syncs the product catalog into the Spring AI VectorStore.
     */
    @PostMapping("/sync-catalog")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncCatalog() {
        int indexedCount = productVectorSyncService.syncAllProducts();
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "indexedCount", indexedCount,
                "status", "SUCCESS",
                "message", "Successfully indexed " + indexedCount + " products into Spring AI VectorStore."
        )));
    }

    @PostMapping("/cancel-order")
    public ResponseEntity<ApiResponse<ChatbotResponseDTO>> cancelOrder(
            Principal principal,
            @RequestBody CancelOrderRequest request) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = getUserFromPrincipal(principal);
        String uuid = request.getOrderId();

        if (uuid != null && uuid.length() == 8) {
            final String target = uuid;
            List<Order> matching = orderRepository.findByUserId(user.getId(), PageRequest.of(0, 100))
                    .getContent()
                    .stream()
                    .filter(o -> o.getOrderUuid().substring(0, 8).equalsIgnoreCase(target))
                    .collect(Collectors.toList());
            if (!matching.isEmpty()) {
                uuid = matching.get(0).getOrderUuid();
            }
        }

        try {
            orderService.cancelOrder(uuid, user.getId());
            return ResponseEntity.ok(ApiResponse.success(new ChatbotResponseDTO(
                    "Order " + (uuid != null && uuid.length() > 8 ? uuid.substring(0, 8) : uuid) + " has been cancelled successfully. A confirmation email has been sent.",
                    "text",
                    null,
                    List.of("🚚 Track my order", "🗣️ Talk to human"),
                    null
            )));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success(new ChatbotResponseDTO(
                    "Failed to cancel order: " + e.getMessage(),
                    "text",
                    null,
                    List.of("🗣️ Talk to human", "🚚 Track my order"),
                    null
            )));
        }
    }

    @GetMapping("/return-policy")
    public ResponseEntity<ApiResponse<ReturnPolicyDTO>> getReturnPolicy() {
        return ResponseEntity.ok(ApiResponse.success(new ReturnPolicyDTO(
                7,
                List.of(
                        "Items must be unused and in their original packaging.",
                        "All tags, warranty cards, and user manuals must be intact.",
                        "Electronics are subject to standard inspection on pickup."
                )
        )));
    }

    private User getUserFromPrincipal(Principal principal) {
        return com.swiftcart.security.SecurityUtil.getUserFromPrincipal(principal, userRepository);
    }
}
