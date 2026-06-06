package com.swiftcart.controller;

import com.swiftcart.dto.RazorpayOrderResponse;
import com.swiftcart.dto.PaymentVerifyRequest;
import com.swiftcart.dto.PaymentVerifyResponse;
import com.swiftcart.dto.RefundResponse;
import com.swiftcart.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<RazorpayOrderResponse> createRazorpayOrder(@RequestBody Map<String, String> body) {
        String orderUuid = body.get("orderUuid");
        if (orderUuid == null) {
            throw new RuntimeException("orderUuid is required in request body");
        }
        RazorpayOrderResponse response = paymentService.createRazorpayOrder(orderUuid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<PaymentVerifyResponse> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        PaymentVerifyResponse response = paymentService.verifyPayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/razorpay/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/razorpay/refund/{orderUuid}")
    public ResponseEntity<RefundResponse> initiateRefund(
            @PathVariable String orderUuid,
            @RequestParam BigDecimal refundAmount) {
        RefundResponse response = paymentService.initiateRefund(orderUuid, refundAmount);
        return ResponseEntity.ok(response);
    }
}
