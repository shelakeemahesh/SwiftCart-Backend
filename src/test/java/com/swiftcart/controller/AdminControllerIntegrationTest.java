package com.swiftcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftcart.entity.*;
import com.swiftcart.enums.*;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private RazorpayPaymentRepository razorpayPaymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Category testCategory;
    private Product testProduct;
    private Coupon testCoupon;

    @BeforeEach
    public void setup() {
        cartRepository.deleteAll();
        reviewRepository.deleteAll();
        flashSaleRepository.deleteAll();
        couponRepository.deleteAll();
        razorpayPaymentRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .phone("9876543210")
                .email("customer@swiftcart.com")
                .name("John Customer")
                .role(Role.CUSTOMER)
                .isVerified(true)
                .build());

        testCategory = categoryRepository.save(Category.builder()
                .name("Electronics")
                .slug("electronics")
                .isActive(true)
                .build());

        User seller = userRepository.save(User.builder()
                .phone("9876543211")
                .email("seller@swiftcart.com")
                .name("Jack Seller")
                .role(Role.SELLER)
                .isVerified(true)
                .build());

        testProduct = productRepository.save(Product.builder()
                .seller(seller)
                .category(testCategory)
                .brand("BrandX")
                .name("Test Product")
                .slug("test-product")
                .description("Test description")
                .basePrice(BigDecimal.valueOf(100.00))
                .mrp(BigDecimal.valueOf(120.00))
                .stockQty(10)
                .isActive(false) 
                .build());

        testCoupon = couponRepository.save(Coupon.builder()
                .code("WELCOME10")
                .type(CouponType.PERCENT)
                .value(BigDecimal.valueOf(10.00))
                .minOrderValue(BigDecimal.valueOf(50.00))
                .usageLimit(100)
                .isActive(true)
                .build());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testListUsers_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    public void testListUsers_NonAdmin_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testChangeUserRole_Success() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/" + testUser.getId() + "/role")
                        .param("role", "SELLER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role", is("SELLER")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeactivateUser_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/" + testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message", is("User deactivated successfully")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testListProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testApproveProduct_Success() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/" + testProduct.getId() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active", is(true)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testRejectProduct_Success() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/" + testProduct.getId() + "/reject")
                        .param("reason", "Incomplete specs details")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active", is(false)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetSalesAnalytics_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/sales")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSalesRevenue", notNullValue()))
                .andExpect(jsonPath("$.data.totalOrdersCount", notNullValue()))
                .andExpect(jsonPath("$.data.activeOrdersCount", notNullValue()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetTopProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(instanceOf(java.util.List.class))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testListAllCoupons_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/coupons")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreateCoupon_Success() throws Exception {
        Coupon newCoupon = Coupon.builder()
                .code("SUPER50")
                .type(CouponType.FLAT)
                .value(BigDecimal.valueOf(50.00))
                .minOrderValue(BigDecimal.valueOf(200.00))
                .usageLimit(50)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/v1/admin/coupons")
                        .content(objectMapper.writeValueAsString(newCoupon))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code", is("SUPER50")))
                .andExpect(jsonPath("$.data.value", is(50.0)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testEditCoupon_Success() throws Exception {
        Coupon editReq = Coupon.builder()
                .code("WELCOME15")
                .type(CouponType.PERCENT)
                .value(BigDecimal.valueOf(15.00))
                .minOrderValue(BigDecimal.valueOf(60.00))
                .usageLimit(150)
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/v1/admin/coupons/" + testCoupon.getId())
                        .content(objectMapper.writeValueAsString(editReq))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code", is("WELCOME15")))
                .andExpect(jsonPath("$.data.value", is(15.0)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeactivateCoupon_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/coupons/" + testCoupon.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message", is("Coupon deactivated successfully")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testListAllFlashSales_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/flash-sales")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(instanceOf(java.util.List.class))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreateFlashSale_Success() throws Exception {
        FlashSale flashSale = FlashSale.builder()
                .product(testProduct)
                .salePrice(BigDecimal.valueOf(80.00))
                .startsAt(LocalDateTime.now().plusDays(1))
                .endsAt(LocalDateTime.now().plusDays(2))
                .stockLimit(5)
                .build();

        mockMvc.perform(post("/api/v1/admin/flash-sales")
                        .content(objectMapper.writeValueAsString(flashSale))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.salePrice", is(80.0)))
                .andExpect(jsonPath("$.data.stockLimit", is(5)));
    }
}
