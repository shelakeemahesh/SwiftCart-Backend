package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

import com.swiftcart.dto.request.OrderRequest;
import com.swiftcart.dto.response.ActiveOrderDTO;
import com.swiftcart.dto.response.OrderTrackingDTO;
import com.swiftcart.dto.response.OrderTrackingDTO.TimelineStepDTO;
import com.swiftcart.dto.response.OrderTrackingDTO.TrackingItemDTO;
import com.swiftcart.entity.Order;
import com.swiftcart.entity.OrderItem;
import com.swiftcart.entity.Address;
import com.swiftcart.enums.OrderStatus;
import com.swiftcart.entity.User;
import com.swiftcart.enums.Role;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.service.DeliveryService;
import com.swiftcart.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final DeliveryService deliveryService;

    public OrderController(OrderService orderService, UserRepository userRepository, DeliveryService deliveryService) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> placeOrder(Principal principal, @Valid @RequestBody OrderRequest request) {
        User user = getUserFromPrincipal(principal);
        Order order = orderService.placeOrder(
                user.getId(),
                request.getAddressId(),
                request.getCouponCode(),
                request.getPaymentMethod(),
                request.getNotes()
        );
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Order>>> listOrders(
            Principal principal,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = getUserFromPrincipal(principal);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(ApiResponse.success(orderService.listUserOrders(user.getId(), status, pageRequest)));
    }

    @GetMapping("/{orderUuid}")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<ApiResponse<Order>> getOrderDetail(Principal principal, @PathVariable String orderUuid) {
        Order order = orderService.getOrderDetail(orderUuid);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{orderUuid}/cancel")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(orderUuid, user.getId())));
    }

    @PostMapping("/{orderUuid}/return")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<ApiResponse<Order>> requestReturn(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(orderService.requestReturn(orderUuid, user.getId())));
    }

    @GetMapping(value = "/{orderUuid}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<byte[]> downloadInvoice(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        Order order = orderService.getOrderDetail(orderUuid);

        // Generating a lightweight valid PDF header structure in memory containing order info
        String invoiceText = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R >>\nendobj\n" +
                "4 0 obj\n<< /Length 150 >>\nstream\n" +
                "BT\n/F1 12 Tf\n70 800 Td\n(SwiftCart Invoice PDF - UUID: " + orderUuid + ") Tj\n" +
                "0 -20 Td\n(Customer Name: " + user.getName() + ") Tj\n" +
                "0 -20 Td\n(Total Amount: Rs. " + order.getFinalAmount() + ") Tj\n" +
                "0 -20 Td\n(Payment Method: " + order.getPaymentMethod() + ") Tj\n" +
                "ET\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f\n" +
                "0000000009 00000 n\n0000000056 00000 n\n0000000111 00000 n\n0000000203 00000 n\n" +
                "trailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n380\n%%EOF";

        byte[] pdfBytes = invoiceText.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + orderUuid + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/{orderUuid}/tracking")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTrackingEvents(Principal principal, @PathVariable String orderUuid) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getShipmentTrackingEvents(orderUuid)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<ActiveOrderDTO>> getActiveOrder(Principal principal) {
        User user = getUserFromPrincipal(principal);
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
                    return ResponseEntity.ok(ApiResponse.success(new ActiveOrderDTO(
                            order.getOrderUuid(),
                            status,
                            productName,
                            productThumbnailUrl,
                            estimatedDelivery,
                            totalItems
                    )));
                })
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{orderUuid}/track")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<ApiResponse<OrderTrackingDTO>> getOrderTracking(Principal principal, @PathVariable String orderUuid) {
        Order order = orderService.getOrderDetail(orderUuid);

        String status = mapStatus(order.getStatus());
        List<TrackingItemDTO> items = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                String name = "";
                String imageUrl = "";
                if (item.getProductSnapshot() != null) {
                    name = item.getProductSnapshot().getName();
                    imageUrl = item.getProductSnapshot().getImageUrl();
                } else if (item.getProduct() != null) {
                    name = item.getProduct().getName();
                    if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                        imageUrl = item.getProduct().getImages().get(0).getImageUrl();
                    }
                }
                items.add(new TrackingItemDTO(
                        name,
                        item.getQuantity(),
                        item.getTotal(),
                        imageUrl
                ));
            }
        }

        Address addr = order.getAddress();
        String deliveryAddress = "";
        if (addr != null) {
            deliveryAddress = String.format("%s, %s, %s, %s - %s",
                    addr.getRecipientName(),
                    addr.getFlatHouse(),
                    addr.getArea(),
                    addr.getCity(),
                    addr.getPincode()
            );
        }

        String estimatedDelivery = order.getPlacedAt().toLocalDate().plusDays(4).toString();
        List<TimelineStepDTO> timeline = buildTimeline(order, status);

        return ResponseEntity.ok(ApiResponse.success(new OrderTrackingDTO(
                order.getOrderUuid(),
                status,
                items,
                deliveryAddress,
                timeline,
                estimatedDelivery
        )));
    }

    private List<TimelineStepDTO> buildTimeline(Order order, String currentMappedStatus) {
        List<TimelineStepDTO> timeline = new ArrayList<>();
        LocalDateTime placedAt = order.getPlacedAt();

        List<String> steps = List.of("PLACED", "CONFIRMED", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED");
        int currentStepIndex = steps.indexOf(currentMappedStatus);

        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            boolean completed = i <= currentStepIndex;
            String timestamp = null;
            if (completed) {
                if (i == 0) {
                    timestamp = placedAt.toString();
                } else if (i == 1) {
                    timestamp = placedAt.plusMinutes(45).toString();
                } else if (i == 2) {
                    timestamp = placedAt.plusHours(12).toString();
                } else if (i == 3) {
                    timestamp = placedAt.plusHours(36).toString();
                } else if (i == 4) {
                    timestamp = placedAt.plusHours(48).toString();
                }
            }
            timeline.add(new TimelineStepDTO(step, timestamp, completed));
        }
        return timeline;
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
        return com.swiftcart.security.SecurityUtil.getUserFromPrincipal(principal, userRepository);
    }
}
