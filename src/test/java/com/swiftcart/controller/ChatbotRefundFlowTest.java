package com.swiftcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftcart.dto.request.ChatbotMessageRequest;
import com.swiftcart.dto.request.RefundOrderRequest;
import com.swiftcart.entity.Order;
import com.swiftcart.entity.User;
import com.swiftcart.enums.OrderStatus;
import com.swiftcart.enums.PaymentMethod;
import com.swiftcart.enums.PaymentStatus;
import com.swiftcart.enums.Role;
import com.swiftcart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatbotRefundFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RazorpayPaymentRepository razorpayPaymentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User customerA;
    private User customerB;
    private Order deliveredOrder;

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

        customerA = userRepository.save(User.builder()
                .email("cust_refund_a@swiftcart.com")
                .name("Refund Customer A")
                .role(Role.CUSTOMER)
                .isVerified(true)
                .build());

        customerB = userRepository.save(User.builder()
                .email("cust_refund_b@swiftcart.com")
                .name("Refund Customer B")
                .role(Role.CUSTOMER)
                .isVerified(true)
                .build());

        deliveredOrder = orderRepository.save(Order.builder()
                .user(customerA)
                .status(OrderStatus.DELIVERED)
                .paymentStatus(PaymentStatus.PAID)
                .paymentMethod(PaymentMethod.CARD)
                .paymentRef("pay_test_refund_ref")
                .mrpTotal(BigDecimal.valueOf(200.00))
                .finalAmount(BigDecimal.valueOf(150.00))
                .build());
    }

    @Test
    @WithMockUser(username = "cust_refund_a@swiftcart.com", roles = {"CUSTOMER"})
    public void testChatbotMessage_RefundIntent_ReturnsRefundableOrderOptions() throws Exception {
        ChatbotMessageRequest request = new ChatbotMessageRequest();
        request.setIntent("REFUND");

        String shortUuid = deliveredOrder.getOrderUuid().substring(0, 8);

        mockMvc.perform(post("/api/v1/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.options", hasItem("Refund " + shortUuid)));
    }

    @Test
    @WithMockUser(username = "cust_refund_a@swiftcart.com", roles = {"CUSTOMER"})
    public void testRefundOrder_Success_UpdatesStatusAndReturnsRefundId() throws Exception {
        RefundOrderRequest request = new RefundOrderRequest(deliveredOrder.getOrderUuid(), "Defective product");

        mockMvc.perform(post("/api/v1/chat/refund-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.messageText", containsString("Refund initiated successfully")));

        Order updated = orderRepository.findById(deliveredOrder.getId()).orElseThrow();
        assertEquals(PaymentStatus.REFUND_INITIATED, updated.getPaymentStatus());
        assertEquals(OrderStatus.RETURNED, updated.getStatus());
        assertNotNull(updated.getRefundId());

        // Test Idempotency: Calling refund again should succeed cleanly
        mockMvc.perform(post("/api/v1/chat/refund-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.messageText", containsString("Refund initiated successfully")));
    }

    @Test
    @WithMockUser(username = "cust_refund_b@swiftcart.com", roles = {"CUSTOMER"})
    public void testRefundOrder_UnauthorizedCustomer_Rejected() throws Exception {
        RefundOrderRequest request = new RefundOrderRequest(deliveredOrder.getOrderUuid(), "Not my order");

        mockMvc.perform(post("/api/v1/chat/refund-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageText", containsString("Failed to process refund")));

        // Verify order state unchanged
        Order unchanged = orderRepository.findById(deliveredOrder.getId()).orElseThrow();
        assertEquals(PaymentStatus.PAID, unchanged.getPaymentStatus());
        assertEquals(OrderStatus.DELIVERED, unchanged.getStatus());
    }

    @Test
    public void testRefundOrder_Unauthenticated_Returns401() throws Exception {
        RefundOrderRequest request = new RefundOrderRequest(deliveredOrder.getOrderUuid());

        mockMvc.perform(post("/api/v1/chat/refund-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
