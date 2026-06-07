package com.swiftcart.controller;

import com.swiftcart.dto.*;
import com.swiftcart.entity.Order;
import com.swiftcart.entity.OrderItem;
import com.swiftcart.entity.OrderStatus;
import com.swiftcart.entity.User;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatbotController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ChatbotController(OrderService orderService, OrderRepository orderRepository, UserRepository userRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatbotResponseDTO> handleMessage(Principal principal, @RequestBody ChatbotMessageRequest request) {
        String intent = request.getIntent() != null ? request.getIntent().toUpperCase() : "";
        
        List<String> defaultOptions = List.of(
                "🚚 Track my order",
                "❌ Cancel an order",
                "↩️ Return / Refund",
                "💳 Payment issue",
                "📦 Order not received",
                "🔐 Account help",
                "🗣️ Talk to human"
        );

        User user = null;
        if (principal != null) {
            user = getUserFromPrincipal(principal);
        }

        switch (intent) {
            case "TRACK_ORDER":
            case "ORDER_NOT_RECEIVED":
                if (user == null) {
                    return ResponseEntity.ok(new ChatbotResponseDTO(
                            "Please log in to track your active orders.",
                            "text",
                            null,
                            List.of("🔐 Account help"),
                            "/login"
                    ));
                }
                
                return orderService.getLatestActiveOrder(user.getId())
                        .map(order -> {
                            String status = mapStatus(order.getStatus());
                            String productName = "";
                            String productThumbnailUrl = "";
                            if (order.getItems() != null && !order.getItems().isEmpty()) {
                                OrderItem firstItem = order.getItems().get(0);
                                if (firstItem.getProductSnapshot() != null) {
                                    productName = firstItem.getProductSnapshot().getName();
                                    productThumbnailUrl = firstItem.getProductSnapshot().getImageUrl();
                                } else if (firstItem.getProduct() != null) {
                                    productName = firstItem.getProduct().getName();
                                    if (firstItem.getProduct().getImages() != null && !firstItem.getProduct().getImages().isEmpty()) {
                                        productThumbnailUrl = firstItem.getProduct().getImages().get(0).getImageUrl();
                                    }
                                }
                            }
                            String estimatedDelivery = order.getPlacedAt().toLocalDate().plusDays(4).toString();
                            int totalItems = order.getItems() != null ? order.getItems().stream().mapToInt(OrderItem::getQuantity).sum() : 0;
                            ActiveOrderDTO orderDTO = new ActiveOrderDTO(
                                    order.getOrderUuid(),
                                    status,
                                    productName,
                                    productThumbnailUrl,
                                    estimatedDelivery,
                                    totalItems
                            );
                            
                            String message = intent.equals("ORDER_NOT_RECEIVED") 
                                ? "Here is the status of your active order. It is currently " + status + "."
                                : "Here is your latest active order:";
                            
                            return ResponseEntity.ok(new ChatbotResponseDTO(
                                    message,
                                    "order_card",
                                    orderDTO,
                                    List.of("❌ Cancel an order", "↩️ Return / Refund", "🗣️ Talk to human"),
                                    null
                            ));
                        })
                        .orElseGet(() -> ResponseEntity.ok(new ChatbotResponseDTO(
                                "You don't have any active orders right now.",
                                "text",
                                null,
                                List.of("❌ Cancel an order", "↩️ Return / Refund", "🗣️ Talk to human"),
                                null
                        )));

            case "CANCEL_ORDER":
                if (user == null) {
                    return ResponseEntity.ok(new ChatbotResponseDTO(
                            "Please log in to view and cancel your orders.",
                            "text",
                            null,
                            List.of("🔐 Account help"),
                            "/login"
                    ));
                }
                
                List<Order> lastOrders = orderRepository.findByUserId(
                        user.getId(), 
                        PageRequest.of(0, 3, Sort.by("id").descending())
                ).getContent();
                
                if (lastOrders.isEmpty()) {
                    return ResponseEntity.ok(new ChatbotResponseDTO(
                            "You don't have any recent orders to cancel.",
                            "text",
                            null,
                            List.of("↩️ Return / Refund", "🗣️ Talk to human"),
                            null
                    ));
                }
                
                List<String> cancelOptions = lastOrders.stream()
                        .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.CONFIRMED)
                        .map(o -> "Cancel " + o.getOrderUuid().substring(0, 8))
                        .collect(Collectors.toList());
                
                if (cancelOptions.isEmpty()) {
                    return ResponseEntity.ok(new ChatbotResponseDTO(
                            "Your recent orders are already shipped or delivered and cannot be cancelled automatically. Please talk to a human agent.",
                            "text",
                            null,
                            List.of("🗣️ Talk to human", "↩️ Return / Refund"),
                            null
                    ));
                }
                
                return ResponseEntity.ok(new ChatbotResponseDTO(
                        "Please select the order you wish to cancel:",
                        "options",
                        null,
                        cancelOptions,
                        null
                ));

            case "RETURN":
                return ResponseEntity.ok(new ChatbotResponseDTO(
                        "You can return items within 7 days of delivery. Items must be unused with tags intact. Click below to go to our returns page.",
                        "text",
                        null,
                        List.of("🚚 Track my order", "🗣️ Talk to human"),
                        "/info/returns-refunds"
                ));

            case "PAYMENT":
                return ResponseEntity.ok(new ChatbotResponseDTO(
                        "For payment failures or double-debits, the amount is automatically refunded to your source account within 3-5 business days. Please provide your order ID if you need further help.",
                        "text",
                        null,
                        List.of("🚚 Track my order", "🗣️ Talk to human"),
                        null
                ));

            case "ACCOUNT":
            case "ACCOUNT_HELP":
                return ResponseEntity.ok(new ChatbotResponseDTO(
                        "You can manage your account, addresses, and view credentials under your Dashboard. Click below to view.",
                        "text",
                        null,
                        List.of("🚚 Track my order", "🗣️ Talk to human"),
                        "/dashboard"
                ));

            case "COMPLAINT":
                return ResponseEntity.ok(new ChatbotResponseDTO(
                        "We are sorry for the inconvenience. Please raise a complaint or talk directly to our human support representatives.",
                        "text",
                        null,
                        List.of("🗣️ Talk to human"),
                        null
                ));

            case "TALK_TO_HUMAN":
                return ResponseEntity.ok(new ChatbotResponseDTO(
                        "Our customer support team is available 9 AM – 6 PM. Email: support@swiftcart.com. We typically respond within 2 hours.",
                        "text",
                        null,
                        List.of("🚚 Track my order", "↩️ Return / Refund"),
                        null
                ));

            default:
                return ResponseEntity.ok(new ChatbotResponseDTO(
                        "I didn't understand that. Here's what I can help with:",
                        "options",
                        null,
                        defaultOptions,
                        null
                ));
        }
    }

    @PostMapping("/cancel-order")
    public ResponseEntity<ChatbotResponseDTO> cancelOrder(Principal principal, @RequestBody CancelOrderRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = getUserFromPrincipal(principal);
        String uuid = request.getOrderId();
        
        // Find full order UUID if the user passed the 8-char short abbreviation
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
            return ResponseEntity.ok(new ChatbotResponseDTO(
                    "Order " + (uuid != null && uuid.length() > 8 ? uuid.substring(0, 8) : uuid) + " has been cancelled successfully. A confirmation email has been sent.",
                    "text",
                    null,
                    List.of("🚚 Track my order", "🗣️ Talk to human"),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatbotResponseDTO(
                    "Failed to cancel order: " + e.getMessage(),
                    "text",
                    null,
                    List.of("🗣️ Talk to human", "🚚 Track my order"),
                    null
            ));
        }
    }

    @GetMapping("/return-policy")
    public ResponseEntity<ReturnPolicyDTO> getReturnPolicy() {
        return ResponseEntity.ok(new ReturnPolicyDTO(
                7,
                List.of(
                        "Items must be unused and in their original packaging.",
                        "All tags, warranty cards, and user manuals must be intact.",
                        "Electronics are subject to standard inspection on pickup."
                )
        ));
    }

    private String mapStatus(OrderStatus status) {
        if (status == null) return "PLACED";
        return switch (status) {
            case PENDING -> "PLACED";
            case CONFIRMED -> "CONFIRMED";
            case PROCESSING, DISPATCHED -> "SHIPPED";
            case OUT_FOR_DELIVERY -> "OUT_FOR_DELIVERY";
            case DELIVERED -> "DELIVERED";
            case CANCELLED -> "CANCELLED";
            default -> status.name();
        };
    }

    private User getUserFromPrincipal(Principal principal) {
        String username = principal.getName();
        return userRepository.findByPhone(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
