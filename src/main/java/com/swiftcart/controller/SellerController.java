package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

import com.swiftcart.entity.Order;
import com.swiftcart.enums.OrderStatus;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.User;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.swiftcart.enums.Role;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final com.swiftcart.service.OrderService orderService;

    public SellerController(
            ProductService productService,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            com.swiftcart.service.OrderService orderService) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(Principal principal) {
        User seller = getUserFromPrincipal(principal);
        List<Product> products = productRepository.findBySellerId(seller.getId(), PageRequest.of(0, 100));

        BigDecimal totalRevenue = products.stream()
                .map(p -> p.getBasePrice().multiply(BigDecimal.valueOf(p.getSoldCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalUnitsSold = products.stream().mapToInt(Product::getSoldCount).sum();

        List<Product> lowStockProducts = products.stream()
                .filter(p -> p.getStockQty() < 5)
                .collect(Collectors.toList());

        List<Order> recentOrders = orderRepository.findBySellerId(seller.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("totalRevenue", totalRevenue);
        data.put("totalUnitsSold", totalUnitsSold);
        data.put("lowStockCount", lowStockProducts.size());
        data.put("lowStockAlerts", lowStockProducts);
        data.put("recentOrdersCount", recentOrders.size());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getMyProducts(Principal principal) {
        User seller = getUserFromPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(productRepository.findBySellerId(seller.getId(), PageRequest.of(0, 100))));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<Product>> addProduct(Principal principal, @RequestBody Product product) {
        User seller = getUserFromPrincipal(principal);
        product.setSeller(seller);
        return ResponseEntity.ok(ApiResponse.success(productService.saveProduct(product)));
    }

    @PostMapping("/products/{id}/images")
    @PreAuthorize("@swiftSecurity.canManageProduct(#id)")
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            Principal principal,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        User seller = getUserFromPrincipal(principal);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getSeller().getId().equals(seller.getId()) && seller.getRole() != Role.ADMIN) {
            throw new RuntimeException("Unauthorized product modification");
        }

        List<String> urls = productService.uploadProductImages(
                id, file.getBytes(), file.getOriginalFilename(), file.getContentType());
        return ResponseEntity.ok(ApiResponse.success(urls));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("@swiftSecurity.canManageProduct(#id)")
    public ResponseEntity<ApiResponse<Product>> editProduct(Principal principal, @PathVariable Long id, @RequestBody Product productRequest) {
        User seller = getUserFromPrincipal(principal);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existing.getSeller().getId().equals(seller.getId()) && seller.getRole() != Role.ADMIN) {
            throw new RuntimeException("Unauthorized product edit");
        }

        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(existing.getSlug(), productRequest)));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("@swiftSecurity.canManageProduct(#id)")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteProduct(Principal principal, @PathVariable Long id) {
        User seller = getUserFromPrincipal(principal);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existing.getSeller().getId().equals(seller.getId()) && seller.getRole() != Role.ADMIN) {
            throw new RuntimeException("Unauthorized product removal");
        }

        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Product deleted successfully")));
    }

    @PutMapping("/products/{id}/stock")
    @PreAuthorize("@swiftSecurity.canManageProduct(#id)")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateStock(Principal principal, @PathVariable Long id, @RequestParam int qty) {
        User seller = getUserFromPrincipal(principal);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existing.getSeller().getId().equals(seller.getId()) && seller.getRole() != Role.ADMIN) {
            throw new RuntimeException("Unauthorized stock modification");
        }

        productService.updateStock(id, qty);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Stock updated successfully")));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<Order>>> getSellerOrders(Principal principal) {
        User seller = getUserFromPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(orderRepository.findBySellerId(seller.getId())));
    }

    @PutMapping("/orders/{orderUuid}/ship")
    @PreAuthorize("@swiftSecurity.canSellerManageOrder(#orderUuid)")
    public ResponseEntity<ApiResponse<Map<String, String>>> shipItem(
            Principal principal,
            @PathVariable String orderUuid,
            @RequestParam String trackingId) {
        
        orderService.updateOrderStatusBySellerOrAdmin(orderUuid, OrderStatus.DISPATCHED);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "message", "Order marked as shipped",
                "trackingId", trackingId,
                "status", "DISPATCHED"
        )));
    }

    @GetMapping("/{sellerId}/profile")
    @PreAuthorize("@swiftSecurity.isAdminOrSellerOwner(#sellerId)")
    public ResponseEntity<ApiResponse<User>> getSellerProfile(@PathVariable Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        return ResponseEntity.ok(ApiResponse.success(seller));
    }

    private User getUserFromPrincipal(Principal principal) {
        return com.swiftcart.security.SecurityUtil.getUserFromPrincipal(principal, userRepository);
    }
}
