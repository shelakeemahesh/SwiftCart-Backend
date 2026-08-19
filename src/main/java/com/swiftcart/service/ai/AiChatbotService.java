package com.swiftcart.service.ai;

import com.swiftcart.dto.request.AiChatRequest;
import com.swiftcart.dto.response.ActiveOrderDTO;
import com.swiftcart.dto.response.AiChatResponseDTO;
import com.swiftcart.entity.Order;
import com.swiftcart.entity.OrderItem;
import com.swiftcart.entity.User;
import com.swiftcart.enums.OrderStatus;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AiChatbotService {

    private static final Logger log = LoggerFactory.getLogger(AiChatbotService.class);

    private final ProductRagService productRagService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    private static final List<String> DEFAULT_OPTIONS = List.of(
            "🚚 Track my order",
            "❌ Cancel an order",
            "↩️ Return / Refund",
            "💳 Payment issue",
            "📦 Order not received",
            "🔐 Account help",
            "🗣️ Talk to human"
    );

    public AiChatbotService(
            ProductRagService productRagService,
            OrderService orderService,
            OrderRepository orderRepository) {
        this.productRagService = productRagService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    /**
     * Process conversational query from user or structured button intent.
     */
    public AiChatResponseDTO processMessage(User user, AiChatRequest request) {
        String message = request.getMessage() != null ? request.getMessage().trim() : "";
        String intent = request.getIntent() != null ? request.getIntent().toUpperCase() : "";

        // If intent is explicitly provided (e.g. from quick button clicks)
        if (!intent.isBlank()) {
            return handleExplicitIntent(user, intent);
        }

        // If message matches known intent phrases
        String normalized = message.toLowerCase(Locale.ROOT);

        if (isOrderTrackingQuery(normalized)) {
            return handleExplicitIntent(user, "TRACK_ORDER");
        } else if (isOrderCancellationQuery(normalized)) {
            return handleExplicitIntent(user, "CANCEL_ORDER");
        } else if (isReturnRefundQuery(normalized)) {
            return handleExplicitIntent(user, "RETURN");
        } else if (isPaymentQuery(normalized)) {
            return handleExplicitIntent(user, "PAYMENT");
        } else if (isAccountQuery(normalized)) {
            return handleExplicitIntent(user, "ACCOUNT");
        } else if (isHumanSupportQuery(normalized)) {
            return handleExplicitIntent(user, "TALK_TO_HUMAN");
        } else if (!message.isBlank()) {
            // Free-text product question / RAG query
            return productRagService.answerProductQuestion(message, request.getProductId());
        }

        return AiChatResponseDTO.text(
                "Hello! I am SwiftCart's AI Support Assistant. I can help you find products, check specifications, track orders, or answer return questions. How can I help you today?",
                DEFAULT_OPTIONS,
                null
        );
    }

    public AiChatResponseDTO handleExplicitIntent(User user, String intent) {
        switch (intent) {
            case "TRACK_ORDER":
            case "ORDER_NOT_RECEIVED":
                if (user == null) {
                    return AiChatResponseDTO.text(
                            "Please log in to track your active orders.",
                            List.of("🔐 Account help"),
                            "/login"
                    );
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

                            String msg = intent.equals("ORDER_NOT_RECEIVED")
                                    ? "Here is the status of your active order. It is currently " + status + "."
                                    : "Here is your latest active order:";

                            return AiChatResponseDTO.order(msg, orderDTO, List.of("❌ Cancel an order", "↩️ Return / Refund", "🗣️ Talk to human"));
                        })
                        .orElseGet(() -> AiChatResponseDTO.text(
                                "You don't have any active orders right now.",
                                List.of("❌ Cancel an order", "↩️ Return / Refund", "🗣️ Talk to human"),
                                null
                        ));

            case "CANCEL_ORDER":
                if (user == null) {
                    return AiChatResponseDTO.text(
                            "Please log in to view and cancel your orders.",
                            List.of("🔐 Account help"),
                            "/login"
                    );
                }

                List<Order> lastOrders = orderRepository.findByUserId(
                        user.getId(),
                        PageRequest.of(0, 3, Sort.by("id").descending())
                ).getContent();

                if (lastOrders.isEmpty()) {
                    return AiChatResponseDTO.text(
                            "You don't have any recent orders to cancel.",
                            List.of("↩️ Return / Refund", "🗣️ Talk to human"),
                            null
                    );
                }

                List<String> cancelOptions = lastOrders.stream()
                        .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.CONFIRMED)
                        .map(o -> {
                            String uuid = o.getOrderUuid();
                            String shortUuid = (uuid != null && uuid.length() >= 8) ? uuid.substring(0, 8) : (uuid != null ? uuid : String.valueOf(o.getId()));
                            return "Cancel " + shortUuid;
                        })
                        .collect(Collectors.toList());

                if (cancelOptions.isEmpty()) {
                    return AiChatResponseDTO.text(
                            "Your recent orders are already shipped or delivered and cannot be cancelled automatically. Please talk to a human agent.",
                            List.of("🗣️ Talk to human", "↩️ Return / Refund"),
                            null
                    );
                }

                return new AiChatResponseDTO(
                        "Please select the order you wish to cancel:",
                        "options",
                        List.of(),
                        null,
                        cancelOptions,
                        null
                );

            case "RETURN":
            case "REFUND":
                return AiChatResponseDTO.text(
                        "You can return items within 7 days of delivery. Items must be unused with original tags and packaging intact. Click below to view our full returns policy.",
                        List.of("🚚 Track my order", "🗣️ Talk to human"),
                        "/info/returns-refunds"
                );

            case "PAYMENT":
                return AiChatResponseDTO.text(
                        "For payment failures or double-debits, the amount is automatically refunded to your source payment method within 3-5 business days.",
                        List.of("🚚 Track my order", "🗣️ Talk to human"),
                        null
                );

            case "ACCOUNT":
            case "ACCOUNT_HELP":
                return AiChatResponseDTO.text(
                        "You can manage your profile, saved shipping addresses, and security credentials under your Account Dashboard.",
                        List.of("🚚 Track my order", "🗣️ Talk to human"),
                        "/dashboard"
                );

            case "TALK_TO_HUMAN":
                return AiChatResponseDTO.text(
                        "Our customer support team is available Monday through Saturday, 9 AM – 6 PM. Email: support@swiftcart.com. Typical response time is under 2 hours.",
                        List.of("🚚 Track my order", "↩️ Return / Refund"),
                        null
                );

            default:
                // If it's a natural query passed as intent, fallback to RAG
                String queryText = (user != null ? "" : "") + intent;
                return productRagService.answerProductQuestion(queryText, null);
        }
    }

    private boolean isOrderTrackingQuery(String text) {
        if (text == null) return false;
        return text.matches("(?i).*\\b(track\\s+(my\\s+)?order|where\\s+is\\s+my\\s+(order|package|delivery)|order\\s+status|tracking\\s+status|package\\s+status)\\b.*");
    }

    private boolean isOrderCancellationQuery(String text) {
        if (text == null) return false;
        return text.matches("(?i)^(\\s*cancel(\\s+(my\\s+)?order)?\\s*)$")
                || text.matches("(?i).*\\b(cancel\\s+(my\\s+)?order|how\\s+to\\s+cancel\\s+(my\\s+)?order)\\b.*");
    }

    private boolean isReturnRefundQuery(String text) {
        if (text == null) return false;
        return text.matches("(?i).*\\b(return\\s+policy|refund\\s+policy|how\\s+to\\s+return|initiate\\s+a?\\s*return|request\\s+a?\\s*refund)\\b.*");
    }

    private boolean isPaymentQuery(String text) {
        if (text == null) return false;
        return text.matches("(?i).*\\b(payment\\s+fail(ed|ure)?|double\\s+debit|money\\s+deducted|transaction\\s+fail(ed|ure)?|payment\\s+issue)\\b.*");
    }

    private boolean isAccountQuery(String text) {
        if (text == null) return false;
        return text.matches("(?i).*\\b(account\\s+help|reset\\s+password|my\\s+profile\\s+settings|change\\s+my\\s+address)\\b.*");
    }

    private boolean isHumanSupportQuery(String text) {
        if (text == null) return false;
        return text.matches("(?i).*\\b(talk\\s+to\\s+(a\\s+)?human|contact\\s+(customer\\s+)?support|speak\\s+with\\s+(an?\\s+)?agent|customer\\s+care\\s+number)\\b.*");
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
}
