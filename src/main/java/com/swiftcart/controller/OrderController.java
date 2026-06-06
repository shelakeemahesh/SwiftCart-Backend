package com.swiftcart.controller;

import com.swiftcart.dto.OrderRequest;
import com.swiftcart.entity.Order;
import com.swiftcart.entity.OrderStatus;
import com.swiftcart.entity.User;
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

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Order> placeOrder(Principal principal, @Valid @RequestBody OrderRequest request) {
        User user = getUserFromPrincipal(principal);
        Order order = orderService.placeOrder(
                user.getId(),
                request.getAddressId(),
                request.getCouponCode(),
                request.getPaymentMethod(),
                request.getNotes()
        );
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<Page<Order>> listOrders(
            Principal principal,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = getUserFromPrincipal(principal);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(orderService.listUserOrders(user.getId(), status, pageRequest));
    }

    @GetMapping("/{orderUuid}")
    public ResponseEntity<Order> getOrderDetail(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        Order order = orderService.getOrderDetail(orderUuid);

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized order access");
        }

        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderUuid}/cancel")
    public ResponseEntity<Order> cancelOrder(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(orderService.cancelOrder(orderUuid, user.getId()));
    }

    @PostMapping("/{orderUuid}/return")
    public ResponseEntity<Order> requestReturn(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(orderService.requestReturn(orderUuid, user.getId()));
    }

    @GetMapping(value = "/{orderUuid}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadInvoice(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        Order order = orderService.getOrderDetail(orderUuid);

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized invoice download");
        }

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
    public ResponseEntity<List<Map<String, Object>>> getTrackingEvents(Principal principal, @PathVariable String orderUuid) {
        User user = getUserFromPrincipal(principal);
        Order order = orderService.getOrderDetail(orderUuid);

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized tracking access");
        }

        return ResponseEntity.ok(deliveryService.getShipmentTrackingEvents(orderUuid));
    }

    private User getUserFromPrincipal(Principal principal) {
        String username = principal.getName();
        return userRepository.findByPhone(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
