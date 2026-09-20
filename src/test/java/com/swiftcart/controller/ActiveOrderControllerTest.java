package com.swiftcart.controller;

import com.swiftcart.entity.*;
import com.swiftcart.enums.*;
import com.swiftcart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ActiveOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RazorpayPaymentRepository razorpayPaymentRepository;

    @Autowired
    private AddressRepository addressRepository;

    private User customer;
    private User seller;
    private Product product;

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

        customer = userRepository.save(User.builder()
                .email("active_customer@swiftcart.com")
                .name("Active Customer")
                .role(Role.CUSTOMER)
                .isVerified(true)
                .build());

        seller = userRepository.save(User.builder()
                .email("active_seller@swiftcart.com")
                .name("Active Seller")
                .role(Role.SELLER)
                .isVerified(true)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Active Category")
                .slug("active-category")
                .isActive(true)
                .build());

        product = Product.builder()
                .seller(seller)
                .category(category)
                .name("Active Gadget")
                .slug("active-gadget")
                .basePrice(BigDecimal.valueOf(100.00))
                .mrp(BigDecimal.valueOf(150.00))
                .stockQty(50)
                .isActive(true)
                .build();
        product.getImages().add(ProductImage.builder()
                .product(product)
                .imageUrl("https://example.com/gadget.jpg")
                .isPrimary(true)
                .displayOrder(0)
                .build());
        product = productRepository.save(product);
    }

    @Test
    @WithMockUser(username = "active_customer@swiftcart.com", roles = {"CUSTOMER"})
    public void testNoActiveOrder_Returns200WithNullData() throws Exception {
        mockMvc.perform(get("/api/v1/orders/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    @WithMockUser(username = "active_customer@swiftcart.com", roles = {"CUSTOMER"})
    public void testActiveOrderPresent_Returns200WithActiveOrderDTO() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .user(customer)
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .paymentMethod(PaymentMethod.CARD)
                .mrpTotal(BigDecimal.valueOf(300.00))
                .finalAmount(BigDecimal.valueOf(200.00))
                .build());

        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .mrp(BigDecimal.valueOf(150.00))
                .discount(BigDecimal.valueOf(50.00))
                .total(BigDecimal.valueOf(200.00))
                .build());

        mockMvc.perform(get("/api/v1/orders/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.orderId", is(order.getOrderUuid())))
                .andExpect(jsonPath("$.data.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.data.productName", is("Active Gadget")))
                .andExpect(jsonPath("$.data.productThumbnailUrl", is("https://example.com/gadget.jpg")))
                .andExpect(jsonPath("$.data.totalItems", is(2)));
    }

    @Test
    @WithMockUser(username = "active_customer@swiftcart.com", roles = {"CUSTOMER"})
    public void testDeliveredOrder_NotReturnedInActiveOrder() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .user(customer)
                .status(OrderStatus.DELIVERED)
                .paymentStatus(PaymentStatus.PAID)
                .paymentMethod(PaymentMethod.CARD)
                .mrpTotal(BigDecimal.valueOf(150.00))
                .finalAmount(BigDecimal.valueOf(100.00))
                .build());

        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(100.00))
                .mrp(BigDecimal.valueOf(150.00))
                .discount(BigDecimal.valueOf(50.00))
                .total(BigDecimal.valueOf(100.00))
                .build());

        mockMvc.perform(get("/api/v1/orders/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    @WithMockUser(username = "active_customer@swiftcart.com", roles = {"CUSTOMER"})
    public void testOrderTracking_Success() throws Exception {
        Address address = addressRepository.save(Address.builder()
                .user(customer)
                .recipientName("Active Customer")
                .flatHouse("Flat 101")
                .area("Tech Park")
                .city("Bangalore")
                .state("KA")
                .pincode("560001")
                .phone("9999999999")
                .build());

        Order order = orderRepository.save(Order.builder()
                .user(customer)
                .address(address)
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .paymentMethod(PaymentMethod.CARD)
                .mrpTotal(BigDecimal.valueOf(150.00))
                .finalAmount(BigDecimal.valueOf(100.00))
                .build());

        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(100.00))
                .mrp(BigDecimal.valueOf(150.00))
                .discount(BigDecimal.valueOf(50.00))
                .total(BigDecimal.valueOf(100.00))
                .build());

        mockMvc.perform(get("/api/v1/orders/" + order.getOrderUuid() + "/track"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.orderId", is(order.getOrderUuid())))
                .andExpect(jsonPath("$.data.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.data.items[0].name", is("Active Gadget")));
    }
}
