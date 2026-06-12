package com.swiftcart;

import com.swiftcart.dto.request.PaymentVerifyRequest;
import com.swiftcart.dto.response.PaymentVerifyResponse;
import com.swiftcart.entity.Order;
import com.swiftcart.enums.PaymentStatus;
import com.swiftcart.entity.User;
import com.swiftcart.repository.OrderRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.repository.RazorpayPaymentRepository;
import com.swiftcart.repository.ReviewRepository;
import com.swiftcart.repository.AddressRepository;
import com.swiftcart.repository.CategoryRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.CartRepository;
import com.swiftcart.service.PaymentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RazorpayPaymentRepository razorpayPaymentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    private Order order;

    @BeforeEach
    public void setup() {
        cartRepository.deleteAll();
        reviewRepository.deleteAll();
        razorpayPaymentRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.builder()
                .phone("9876543210")
                .name("Test User")
                .build());

        order = orderRepository.save(Order.builder()
                .user(user)
                .finalAmount(BigDecimal.valueOf(250.00))
                .razorpayOrderId("order_test123")
                .paymentStatus(PaymentStatus.PENDING)
                .build());
    }

    @Test
    public void testVerifyPaymentSignature_Success() {
        String razorpayOrderId = "order_test123";
        String razorpayPaymentId = "pay_test456";
        
        // Compute correct signature using the configured secret
        String secret = razorpayKeySecret;
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        String signature = calculateHmacSha256(payload, secret);

        PaymentVerifyRequest request = new PaymentVerifyRequest(
                razorpayPaymentId,
                razorpayOrderId,
                signature,
                order.getOrderUuid()
        );

        PaymentVerifyResponse response = paymentService.verifyPayment(request);
        
        Assertions.assertTrue(response.isSuccess());

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        Assertions.assertEquals(PaymentStatus.PAID, updatedOrder.getPaymentStatus());
        Assertions.assertEquals(razorpayPaymentId, updatedOrder.getPaymentRef());
    }

    @Test
    public void testVerifyPaymentSignature_Failure() {
        PaymentVerifyRequest request = new PaymentVerifyRequest(
                "pay_test456",
                "order_test123",
                "invalid_sig_xxxx",
                order.getOrderUuid()
        );

        Assertions.assertThrows(RuntimeException.class, () -> {
            paymentService.verifyPayment(request);
        });
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
}
