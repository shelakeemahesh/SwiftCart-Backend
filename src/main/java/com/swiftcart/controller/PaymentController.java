package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;
import com.swiftcart.exception.BadRequestException;

import com.swiftcart.dto.response.RazorpayOrderResponse;
import com.swiftcart.dto.request.PaymentVerifyRequest;
import com.swiftcart.dto.response.PaymentVerifyResponse;
import com.swiftcart.dto.response.RefundResponse;
import com.swiftcart.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/razorpay/create-order")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#body['orderUuid'])")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createRazorpayOrder(@RequestBody Map<String, String> body) {
        String orderUuid = body.get("orderUuid");
        if (orderUuid == null) {
            throw new BadRequestException("orderUuid is required in request body");
        }
        RazorpayOrderResponse response = paymentService.createRazorpayOrder(orderUuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/razorpay/verify")
    @PreAuthorize("@swiftSecurity.canCustomerManageOrder(#request.swiftcartOrderUuid)")
    public ResponseEntity<ApiResponse<PaymentVerifyResponse>> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        PaymentVerifyResponse response = paymentService.verifyPayment(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/razorpay/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signatureHeader,
            @RequestHeader Map<String, String> headers) {
        String signature = signatureHeader;
        if (signature == null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if ("x-razorpay-signature".equalsIgnoreCase(entry.getKey())) {
                    signature = entry.getValue();
                    break;
                }
            }
        }
        paymentService.processWebhook(payload, signature);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/razorpay/refund/{orderUuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RefundResponse>> initiateRefund(
            @PathVariable String orderUuid,
            @RequestParam BigDecimal refundAmount) {
        RefundResponse response = paymentService.initiateRefund(orderUuid, refundAmount);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
