package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;
import com.swiftcart.entity.PriceDropAlert;
import com.swiftcart.entity.ProductPriceHistory;
import com.swiftcart.entity.User;
import com.swiftcart.exception.BadRequestException;
import com.swiftcart.exception.UnauthorizedException;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.service.PriceHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;
    private final UserRepository userRepository;

    public PriceHistoryController(
            PriceHistoryService priceHistoryService,
            UserRepository userRepository) {
        this.priceHistoryService = priceHistoryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{slug}/price-history")
    public ResponseEntity<ApiResponse<List<ProductPriceHistory>>> getPriceHistory(@PathVariable String slug) {
        List<ProductPriceHistory> history = priceHistoryService.getOrCreatePriceHistory(slug);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @PostMapping("/{slug}/alerts")
    public ResponseEntity<ApiResponse<PriceDropAlert>> createPriceDropAlert(
            Principal principal,
            @PathVariable String slug,
            @RequestBody Map<String, Object> body) {
        
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to create price alerts");
        }

        if (body == null || body.get("targetPrice") == null) {
            throw new BadRequestException("targetPrice is required");
        }

        User user = com.swiftcart.security.SecurityUtil.getUserFromPrincipal(principal, userRepository);

        BigDecimal targetPrice;
        try {
            targetPrice = new BigDecimal(body.get("targetPrice").toString());
        } catch (NumberFormatException e) {
            throw new BadRequestException("targetPrice must be a valid number");
        }

        if (targetPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("targetPrice must be greater than zero");
        }

        String email = body.getOrDefault("email", user.getEmail()).toString();

        PriceDropAlert alert = priceHistoryService.createAlert(user.getId(), slug, targetPrice, email);
        return ResponseEntity.ok(ApiResponse.success(alert));
    }
}
