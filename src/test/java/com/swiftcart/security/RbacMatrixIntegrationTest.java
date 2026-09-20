package com.swiftcart.security;

import com.swiftcart.entity.Category;
import com.swiftcart.entity.Order;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.User;
import com.swiftcart.enums.OrderStatus;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RbacMatrixIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RazorpayPaymentRepository razorpayPaymentRepository;

    @Autowired
    private AddressRepository addressRepository;

    private User customerA;
    private User customerB;
    private User sellerA;
    private User sellerB;
    private Order orderCustomerA;
    private Order orderCustomerB;
    private Product productSellerB;

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
                .email("customera@swiftcart.com")
                .name("Customer A")
                .role(Role.CUSTOMER)
                .isVerified(true)
                .build());

        customerB = userRepository.save(User.builder()
                .email("customerb@swiftcart.com")
                .name("Customer B")
                .role(Role.CUSTOMER)
                .isVerified(true)
                .build());

        sellerA = userRepository.save(User.builder()
                .email("sellera@swiftcart.com")
                .name("Seller A")
                .role(Role.SELLER)
                .isVerified(true)
                .build());

        sellerB = userRepository.save(User.builder()
                .email("sellerb@swiftcart.com")
                .name("Seller B")
                .role(Role.SELLER)
                .isVerified(true)
                .build());

        userRepository.save(User.builder()
                .email("admin@swiftcart.com")
                .name("Admin")
                .role(Role.ADMIN)
                .isVerified(true)
                .build());

        orderCustomerA = orderRepository.save(Order.builder()
                .user(customerA)
                .finalAmount(BigDecimal.valueOf(100.00))
                .paymentStatus(PaymentStatus.PAID)
                .status(OrderStatus.CONFIRMED)
                .build());

        orderCustomerB = orderRepository.save(Order.builder()
                .user(customerB)
                .finalAmount(BigDecimal.valueOf(200.00))
                .paymentStatus(PaymentStatus.PAID)
                .status(OrderStatus.CONFIRMED)
                .build());

        Category testCategory = categoryRepository.save(Category.builder()
                .name("Test Category")
                .slug("test-category")
                .isActive(true)
                .build());

        productSellerB = productRepository.save(Product.builder()
                .seller(sellerB)
                .category(testCategory)
                .name("Seller B Product")
                .slug("seller-b-product")
                .basePrice(BigDecimal.valueOf(50.00))
                .stockQty(10)
                .isActive(true)
                .build());
    }

    @Test
    public void testUnauthenticatedRequest_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "customera@swiftcart.com", roles = {"CUSTOMER"})
    public void testCustomer_AccessOwnOrder_Success() throws Exception {
        mockMvc.perform(get("/api/v1/orders/" + orderCustomerA.getOrderUuid()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customera@swiftcart.com", roles = {"CUSTOMER"})
    public void testCustomer_AccessOtherCustomerOrder_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/orders/" + orderCustomerB.getOrderUuid()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customera@swiftcart.com", roles = {"CUSTOMER"})
    public void testCustomer_CancelOtherCustomerOrder_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/orders/" + orderCustomerB.getOrderUuid() + "/cancel"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customera@swiftcart.com", roles = {"CUSTOMER"})
    public void testCustomer_AccessAdminEndpoint_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "sellera@swiftcart.com", roles = {"SELLER"})
    public void testSeller_ManageOtherSellerProductStock_Forbidden() throws Exception {
        mockMvc.perform(put("/api/v1/seller/products/" + productSellerB.getId() + "/stock")
                        .param("qty", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@swiftcart.com", roles = {"ADMIN"})
    public void testAdmin_AccessAdminEndpoint_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@swiftcart.com", roles = {"ADMIN"})
    public void testAdmin_AccessAnyCustomerOrder_Success() throws Exception {
        mockMvc.perform(get("/api/v1/orders/" + orderCustomerA.getOrderUuid()))
                .andExpect(status().isOk());
    }
}
