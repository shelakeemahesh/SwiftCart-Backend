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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        String cleanUuid = orderUuid.replaceAll("[^a-zA-Z0-9-]", "");
        Order order = orderService.getOrderDetail(cleanUuid);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{orderUuid}/cancel")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        String cleanUuid = orderUuid.replaceAll("[^a-zA-Z0-9-]", "");
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(cleanUuid, user.getId())));
    }

    @PostMapping("/{orderUuid}/return")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<ApiResponse<Order>> requestReturn(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        String cleanUuid = orderUuid.replaceAll("[^a-zA-Z0-9-]", "");
        return ResponseEntity.ok(ApiResponse.success(orderService.requestReturn(cleanUuid, user.getId())));
    }

    @GetMapping(value = "/{orderUuid}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<byte[]> downloadInvoice(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        String cleanUuid = orderUuid.replaceAll("[^a-zA-Z0-9-]", "");
        Order order = orderService.getOrderDetail(cleanUuid);

        byte[] pdfBytes = generateInvoicePdf(order, user, cleanUuid);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + cleanUuid + ".pdf\"")
                .body(pdfBytes);
    }

    private byte[] generateInvoicePdf(Order order, User user, String cleanUuid) {
        String customerName = (user != null && user.getName() != null) ? user.getName() : "Customer";
        String amount = (order.getFinalAmount() != null) ? order.getFinalAmount().toPlainString() : "0.00";
        String paymentMethod = (order.getPaymentMethod() != null) ? order.getPaymentMethod().name() : "N/A";
        String date = (order.getPlacedAt() != null) ? order.getPlacedAt().toString() : "";

        customerName = customerName.replace("(", "\\(").replace(")", "\\)");

        StringBuilder streamContent = new StringBuilder();
        streamContent.append("BT\n");
        streamContent.append("/F1 16 Tf\n");
        streamContent.append("50 780 Td (SWIFTCART TAX INVOICE) Tj\n");
        streamContent.append("/F1 11 Tf\n");
        streamContent.append("0 -30 Td (Invoice / Order UUID: ").append(cleanUuid).append(") Tj\n");
        streamContent.append("0 -20 Td (Date: ").append(date).append(") Tj\n");
        streamContent.append("0 -20 Td (Customer: ").append(customerName).append(") Tj\n");
        streamContent.append("0 -20 Td (Payment Method: ").append(paymentMethod).append(") Tj\n");
        streamContent.append("0 -20 Td (Payment Status: ").append(order.getPaymentStatus()).append(") Tj\n");
        streamContent.append("0 -25 Td (Total Amount Paid: Rs. ").append(amount).append(") Tj\n");
        streamContent.append("0 -40 Td (Thank you for shopping with SwiftCart!) Tj\n");
        streamContent.append("ET\n");

        byte[] streamBytes = streamContent.toString().getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            List<Long> offsets = new ArrayList<>();
            baos.write("%PDF-1.4\n".getBytes(StandardCharsets.UTF_8));

            // Object 1: Catalog
            offsets.add((long) baos.size());
            baos.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.UTF_8));

            // Object 2: Pages
            offsets.add((long) baos.size());
            baos.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.UTF_8));

            // Object 3: Page
            offsets.add((long) baos.size());
            baos.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n".getBytes(StandardCharsets.UTF_8));

            // Object 4: Font
            offsets.add((long) baos.size());
            baos.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.UTF_8));

            // Object 5: Content Stream
            offsets.add((long) baos.size());
            String streamHeader = "5 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n";
            baos.write(streamHeader.getBytes(StandardCharsets.UTF_8));
            baos.write(streamBytes);
            baos.write("\nendstream\nendobj\n".getBytes(StandardCharsets.UTF_8));

            // xref
            long startxref = baos.size();
            baos.write(String.format("xref\n0 %d\n0000000000 65535 f \n", offsets.size() + 1).getBytes(StandardCharsets.UTF_8));
            for (Long offset : offsets) {
                baos.write(String.format("%010d 00000 n \n", offset).getBytes(StandardCharsets.UTF_8));
            }

            // trailer
            baos.write(String.format("trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF", offsets.size() + 1, startxref).getBytes(StandardCharsets.UTF_8));
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    @GetMapping("/{orderUuid}/tracking")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTrackingEvents(Principal principal, @PathVariable String orderUuid) {
        String cleanUuid = orderUuid.replaceAll("[^a-zA-Z0-9-]", "");
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getShipmentTrackingEvents(cleanUuid)));
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
                    String estimatedDelivery = order.getPlacedAt() != null 
                            ? order.getPlacedAt().toLocalDate().plusDays(4).toString() 
                            : java.time.LocalDate.now().plusDays(4).toString();
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
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success(null)));
    }

    @GetMapping("/{orderUuid}/track")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#orderUuid)")
    public ResponseEntity<ApiResponse<OrderTrackingDTO>> getOrderTracking(Principal principal, @PathVariable String orderUuid) {
        String cleanUuid = orderUuid.replaceAll("[^a-zA-Z0-9-]", "");
        Order order = orderService.getOrderDetail(cleanUuid);

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

        String estimatedDelivery = order.getPlacedAt() != null 
                ? order.getPlacedAt().toLocalDate().plusDays(4).toString() 
                : java.time.LocalDate.now().plusDays(4).toString();
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
        LocalDateTime placedAt = order.getPlacedAt() != null ? order.getPlacedAt() : LocalDateTime.now();

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
                } else {
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
