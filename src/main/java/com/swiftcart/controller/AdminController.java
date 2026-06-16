package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

import com.swiftcart.enums.*;

import com.swiftcart.entity.*;
import com.swiftcart.repository.CouponRepository;
import com.swiftcart.repository.FlashSaleRepository;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final ProductService productService;

    public AdminController(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            CouponRepository couponRepository,
            FlashSaleRepository flashSaleRepository,
            ProductService productService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.couponRepository = couponRepository;
        this.flashSaleRepository = flashSaleRepository;
        this.productService = productService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(userRepository.findAll(PageRequest.of(page, size))));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> changeUserRole(@PathVariable Long id, @RequestParam Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        return ResponseEntity.ok(ApiResponse.success(userRepository.save(user)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deactivateUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(false);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "User deactivated successfully")));
    }

    @PutMapping("/users/{id}/verify")
    public ResponseEntity<ApiResponse<User>> verifyUser(@PathVariable Long id, @RequestParam boolean verified) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(verified);
        return ResponseEntity.ok(ApiResponse.success(userRepository.save(user)));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<Product>>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(productRepository.findAll(PageRequest.of(page, size))));
    }

    @PutMapping("/products/{id}/approve")
    public ResponseEntity<ApiResponse<Product>> approveProduct(@PathVariable Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setActive(true);
        return ResponseEntity.ok(ApiResponse.success(productService.saveProduct(p)));
    }

    @PutMapping("/products/{id}/reject")
    public ResponseEntity<ApiResponse<Product>> rejectProduct(@PathVariable Long id, @RequestParam String reason) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setActive(false);
        
        return ResponseEntity.ok(ApiResponse.success(productService.saveProduct(p)));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<Order>>> listAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(orderRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))));
    }

    @GetMapping("/analytics/sales")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesAnalytics() {
        List<Order> orders = orderRepository.findAll();
        BigDecimal totalSales = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("totalSalesRevenue", totalSales);
        data.put("totalOrdersCount", orders.size());
        data.put("activeOrdersCount", orders.stream().filter(o -> o.getStatus() == OrderStatus.PROCESSING || o.getStatus() == OrderStatus.DISPATCHED).count());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/analytics/products")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopProducts() {
        List<Product> products = productRepository.findTop20ByIsActiveTrueOrderBySoldCountDesc();
        List<Map<String, Object>> list = products.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", p.getId());
                    map.put("name", p.getName());
                    map.put("unitsSold", p.getSoldCount());
                    map.put("revenue", p.getBasePrice().multiply(BigDecimal.valueOf(p.getSoldCount())));
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<List<Coupon>>> listAllCoupons() {
        return ResponseEntity.ok(ApiResponse.success(couponRepository.findAll()));
    }

    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(@RequestBody Coupon coupon) {
        return ResponseEntity.ok(ApiResponse.success(couponRepository.save(coupon)));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<Coupon>> editCoupon(@PathVariable Long id, @RequestBody Coupon req) {
        Coupon c = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        c.setCode(req.getCode());
        c.setType(req.getType());
        c.setValue(req.getValue());
        c.setMinOrderValue(req.getMinOrderValue());
        c.setMaxDiscount(req.getMaxDiscount());
        c.setUsageLimit(req.getUsageLimit());
        c.setExpiresAt(req.getExpiresAt());
        c.setActive(req.isActive());
        return ResponseEntity.ok(ApiResponse.success(couponRepository.save(c)));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deactivateCoupon(@PathVariable Long id) {
        Coupon c = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        c.setActive(false);
        couponRepository.save(c);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Coupon deactivated successfully")));
    }

    @GetMapping("/flash-sales")
    public ResponseEntity<ApiResponse<List<FlashSale>>> listAllFlashSales() {
        List<FlashSale> sales = flashSaleRepository.findAll();
        
        sales.forEach(fs -> {
            if (fs.getProduct() != null) {
                fs.getProduct().getName();
            }
        });
        return ResponseEntity.ok(ApiResponse.success(sales));
    }

    @PostMapping("/flash-sales")
    public ResponseEntity<ApiResponse<FlashSale>> createFlashSale(@RequestBody FlashSale sale) {
        Product p = productRepository.findById(sale.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        sale.setProduct(p);
        return ResponseEntity.ok(ApiResponse.success(flashSaleRepository.save(sale)));
    }

    @PostMapping("/products/import")
    public ResponseEntity<ApiResponse<Map<String, String>>> importProducts(
            Principal principal,
            @RequestParam("file") MultipartFile file) throws IOException {
        String jobId = UUID.randomUUID().toString();

        User admin = com.swiftcart.security.SecurityUtil.getUserFromPrincipal(principal, userRepository);

        productService.processCsvImport(jobId, file.getBytes(), admin.getId());

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "jobId", jobId,
                "status", "PENDING",
                "message", "Async CSV import task triggered successfully"
        )));
    }

    @GetMapping("/products/import/status/{jobId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getImportStatus(@PathVariable String jobId) {
        String status = productService.getCsvImportStatus(jobId);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "jobId", jobId,
                "status", status
        )));
    }
}
