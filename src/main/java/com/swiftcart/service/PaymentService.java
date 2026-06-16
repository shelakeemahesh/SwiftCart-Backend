package com.swiftcart.service;

import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import com.swiftcart.dto.response.RazorpayOrderResponse;
import com.swiftcart.dto.request.PaymentVerifyRequest;
import com.swiftcart.dto.response.PaymentVerifyResponse;
import com.swiftcart.dto.response.RefundResponse;
import com.swiftcart.entity.Order;
import com.swiftcart.enums.OrderStatus;
import com.swiftcart.enums.PaymentStatus;
import com.swiftcart.entity.RazorpayPayment;
import com.swiftcart.enums.RazorpayPaymentStatus;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.repository.RazorpayPaymentRepository;
import com.swiftcart.service.NotificationService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final OrderRepository orderRepository;
    private final RazorpayPaymentRepository razorpayPaymentRepository;
    private final RazorpayClient razorpayClient;
    private final NotificationService notificationService;
    private final StringRedisTemplate redisTemplate;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.webhook.secret}")
    private String razorpayWebhookSecret;

    public PaymentService(StringRedisTemplate redisTemplate, 
            OrderRepository orderRepository,
            RazorpayPaymentRepository razorpayPaymentRepository,
            RazorpayClient razorpayClient,
            NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.razorpayPaymentRepository = razorpayPaymentRepository;
        this.razorpayClient = razorpayClient;
        this.notificationService = notificationService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public RazorpayOrderResponse createRazorpayOrder(String orderUuid) {
        Order swiftOrder = orderRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            JSONObject orderRequest = new JSONObject();
            
            int amountPaisa = swiftOrder.getFinalAmount().multiply(BigDecimal.valueOf(100)).intValue();
            orderRequest.put("amount", amountPaisa);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", swiftOrder.getOrderUuid());
            orderRequest.put("payment_capture", 1); 

            JSONObject notes = new JSONObject();
            notes.put("swiftcart_order_id", swiftOrder.getOrderUuid());
            notes.put("user_id", swiftOrder.getUser().getId());
            orderRequest.put("notes", notes);

            com.razorpay.Order rzpOrder = razorpayClient.orders.create(orderRequest);
            String rzpOrderId = rzpOrder.get("id");

            swiftOrder.setRazorpayOrderId(rzpOrderId);
            swiftOrder.setPaymentStatus(PaymentStatus.PENDING);
            orderRepository.save(swiftOrder);

            RazorpayPayment payment = RazorpayPayment.builder()
                    .order(swiftOrder)
                    .razorpayOrderId(rzpOrderId)
                    .amountPaisa(amountPaisa)
                    .currency("INR")
                    .status(RazorpayPaymentStatus.CREATED)
                    .build();
            razorpayPaymentRepository.save(payment);

            log.info("Razorpay order created: {} for SwiftCart order UUID: {}", rzpOrderId, orderUuid);

            return new RazorpayOrderResponse(
                    rzpOrderId,
                    swiftOrder.getFinalAmount(),
                    "INR",
                    razorpayKeyId
            );

        } catch (Exception e) {
            log.error("Failed to create Razorpay order for UUID: {}", orderUuid, e);
            throw new RuntimeException("Failed to initiate Razorpay order: " + e.getMessage(), e);
        }
    }

    @Transactional
    public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest req) {
        
        String payload = req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId();

        String expectedSignature = calculateHmacSha256(payload, razorpayKeySecret);
        boolean isValid = expectedSignature.equals(req.getRazorpaySignature());

        if (!isValid) {
            throw new RuntimeException("Signature mismatch — possible tampering");
        }

        Order order = orderRepository.findByOrderUuid(req.getSwiftcartOrderUuid())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentRef(req.getRazorpayPaymentId());
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            if (order.getUser() != null && order.getUser().getEmail() != null) {
                notificationService.sendOrderStatusUpdate(order.getUser().getEmail(), order.getOrderUuid(), "CONFIRMED");
            }
            log.info("Payment verified successfully via signature verification for order UUID: {}", order.getOrderUuid());
        }

        razorpayPaymentRepository.findByRazorpayOrderId(req.getRazorpayOrderId()).ifPresent(payment -> {
            payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
            payment.setRazorpaySignature(req.getRazorpaySignature());
            payment.setStatus(RazorpayPaymentStatus.CAPTURED);
            razorpayPaymentRepository.save(payment);
        });

        return new PaymentVerifyResponse(true, "Payment verified successfully");
    }

    @Transactional
    public void processWebhook(String payload, String signatureHeader) {
        
        boolean isValid = verifyWebhookSignature(payload, signatureHeader, razorpayWebhookSecret);
        if (!isValid) {
            log.warn("Invalid Razorpay webhook signature");
            throw new RuntimeException("Invalid webhook signature, HMAC verification failed");
        }

        String idempotencyKey = "webhook_processed:" + signatureHeader;
        boolean isNew = true;
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "true", Duration.ofHours(24));
            if (result != null) {
                isNew = result;
            }
        } catch (Exception e) {
            log.warn("Redis is unavailable for webhook idempotency check. Proceeding without check. Error: {}", e.getMessage());
        }
        if (!isNew) {
            log.info("Webhook already processed (Idempotency key: {})", signatureHeader);
            return;
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");
        log.info("Processing Razorpay webhook event: {}", eventType);

        switch (eventType) {
            case "payment.captured" -> handlePaymentCaptured(event);
            case "payment.failed"   -> handlePaymentFailed(event);
            case "refund.processed" -> handleRefundProcessed(event);
            case "order.paid"       -> handleOrderPaid(event);
            default -> log.info("Unhandled webhook event type: {}", eventType);
        }
    }

    private void handlePaymentCaptured(JSONObject event) {
        JSONObject paymentEntity = event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        String rzpOrderId = paymentEntity.getString("order_id");
        String rzpPaymentId = paymentEntity.getString("id");
        String method = paymentEntity.optString("method", "unknown");

        orderRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(order -> {
            
            if (order.getPaymentStatus() != PaymentStatus.PAID) {
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setPaymentRef(rzpPaymentId);
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);

                if (order.getUser() != null && order.getUser().getEmail() != null) {
                    notificationService.sendOrderStatusUpdate(order.getUser().getEmail(), order.getOrderUuid(), "CONFIRMED");
                }
                log.info("Webhook marked order {} as PAID (payment captured)", order.getOrderUuid());
            }

            razorpayPaymentRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(payment -> {
                payment.setRazorpayPaymentId(rzpPaymentId);
                payment.setMethod(method);
                payment.setStatus(RazorpayPaymentStatus.CAPTURED);
                payment.setWebhookEvents(event.toString());
                razorpayPaymentRepository.save(payment);
            });
        });
    }

    private void handlePaymentFailed(JSONObject event) {
        JSONObject paymentEntity = event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        String rzpOrderId = paymentEntity.optString("order_id", null);
        String rzpPaymentId = paymentEntity.getString("id");
        String errorDescription = paymentEntity.optString("error_description", "Unknown error");

        if (rzpOrderId != null) {
            orderRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(order -> {
                if (order.getPaymentStatus() == PaymentStatus.PENDING) {
                    order.setPaymentStatus(PaymentStatus.FAILED);
                    orderRepository.save(order);
                    log.info("Webhook marked order {} as FAILED", order.getOrderUuid());
                }

                razorpayPaymentRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(payment -> {
                    payment.setRazorpayPaymentId(rzpPaymentId);
                    payment.setStatus(RazorpayPaymentStatus.FAILED);
                    payment.setFailureReason(errorDescription);
                    payment.setWebhookEvents(event.toString());
                    razorpayPaymentRepository.save(payment);
                });
            });
        }
    }

    private void handleRefundProcessed(JSONObject event) {
        JSONObject refundEntity = event.getJSONObject("payload").getJSONObject("refund").getJSONObject("entity");
        String rzpPaymentId = refundEntity.getString("payment_id");
        String refundId = refundEntity.getString("id");

        razorpayPaymentRepository.findByRazorpayPaymentId(rzpPaymentId).ifPresent(payment -> {
            Order order = payment.getOrder();
            if (order != null) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
                order.setRefundId(refundId);
                orderRepository.save(order);
                log.info("Webhook marked order {} as REFUNDED", order.getOrderUuid());
            }

            payment.setRefundId(refundId);
            payment.setStatus(RazorpayPaymentStatus.REFUNDED);
            payment.setWebhookEvents(event.toString());
            razorpayPaymentRepository.save(payment);
        });
    }

    private void handleOrderPaid(JSONObject event) {
        JSONObject orderEntity = event.getJSONObject("payload").getJSONObject("order").getJSONObject("entity");
        String rzpOrderId = orderEntity.getString("id");

        orderRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(order -> {
            if (order.getPaymentStatus() != PaymentStatus.PAID) {
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);

                if (order.getUser() != null && order.getUser().getEmail() != null) {
                    notificationService.sendOrderStatusUpdate(order.getUser().getEmail(), order.getOrderUuid(), "CONFIRMED");
                }
                log.info("Webhook marked order {} as PAID (order.paid event)", order.getOrderUuid());
            }
        });
    }

    @Transactional
    public RefundResponse initiateRefund(String orderUuid, BigDecimal refundAmount) {
        Order order = orderRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Refund only permitted on PAID orders");
        }

        try {
            JSONObject refundRequest = new JSONObject();
            
            refundRequest.put("amount", refundAmount.multiply(BigDecimal.valueOf(100)).intValue());
            refundRequest.put("speed", "optimum");

            JSONObject notes = new JSONObject();
            notes.put("reason", "customer_return");
            notes.put("swiftcart_order", orderUuid);
            refundRequest.put("notes", notes);

            Refund refund = razorpayClient.payments.refund(order.getPaymentRef(), refundRequest);
            String refundId = refund.get("id");
            String refundStatus = refund.get("status");

            order.setPaymentStatus(PaymentStatus.REFUND_INITIATED);
            order.setRefundId(refundId);
            orderRepository.save(order);

            razorpayPaymentRepository.findByRazorpayOrderId(order.getRazorpayOrderId()).ifPresent(payment -> {
                payment.setRefundId(refundId);
                payment.setStatus(RazorpayPaymentStatus.REFUNDED);
                razorpayPaymentRepository.save(payment);
            });

            log.info("Refund initiated successfully for order UUID: {}. Refund ID: {}", orderUuid, refundId);
            return new RefundResponse(refundId, refundStatus);

        } catch (Exception e) {
            log.error("Failed to process refund for order UUID: {}", orderUuid, e);
            throw new RuntimeException("Razorpay refund failed: " + e.getMessage(), e);
        }
    }

    private String calculateHmacSha256(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKeySpec);
            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC signature", e);
        }
    }

    private boolean verifyWebhookSignature(String payload, String signature, String secret) {
        try {
            String expected = calculateHmacSha256(payload, secret);
            return expected.equalsIgnoreCase(signature) || signature.contains(expected);
        } catch (Exception e) {
            return false;
        }
    }
}
