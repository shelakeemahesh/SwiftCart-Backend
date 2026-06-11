package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

import com.swiftcart.entity.Coupon;
import com.swiftcart.entity.User;
import com.swiftcart.repository.CouponRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.service.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final PricingService pricingService;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public CouponController(PricingService pricingService, CouponRepository couponRepository, UserRepository userRepository) {
        this.pricingService = pricingService;
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateCoupon(Principal principal, @RequestBody Map<String, Object> body) {
        User user = getUserFromPrincipal(principal);
        String code = (String) body.get("code");
        BigDecimal orderValue = new BigDecimal(body.get("orderValue").toString());

        Coupon coupon = pricingService.validateCoupon(code, orderValue, user.getId());
        BigDecimal discount = pricingService.calculateCouponDiscount(coupon, orderValue);

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "valid", true,
                "code", coupon.getCode(),
                "discount", discount,
                "type", coupon.getType().name()
        )));
    }

    @GetMapping("/my-coupons")
    public ResponseEntity<ApiResponse<List<Coupon>>> getMyCoupons(Principal principal) {
        User user = getUserFromPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.success(couponRepository.findAvailableCoupons(user.getId(), LocalDateTime.now())));
    }

    private User getUserFromPrincipal(Principal principal) {
        String username = principal.getName();
        return userRepository.findByPhone(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
