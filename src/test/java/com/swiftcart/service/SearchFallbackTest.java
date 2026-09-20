package com.swiftcart.service;

import com.swiftcart.entity.Category;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.ProductDocument;
import com.swiftcart.entity.User;
import com.swiftcart.enums.Role;
import com.swiftcart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SearchFallbackTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RazorpayPaymentRepository razorpayPaymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    private Product laptop;
    private Product headphones;

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

        User seller = userRepository.save(User.builder()
                .email("seller_search@swiftcart.com")
                .name("Search Seller")
                .role(Role.SELLER)
                .isVerified(true)
                .build());

        Category electronics = categoryRepository.save(Category.builder()
                .name("Electronics")
                .slug("electronics")
                .isActive(true)
                .build());

        laptop = productRepository.save(Product.builder()
                .seller(seller)
                .category(electronics)
                .name("UltraBook Pro 15")
                .brand("TechBrand")
                .description("High-performance laptop with 16GB RAM")
                .basePrice(BigDecimal.valueOf(1000.00))
                .mrp(BigDecimal.valueOf(1200.00))
                .stockQty(15)
                .isActive(true)
                .slug("ultrabook-pro-15")
                .build());

        headphones = productRepository.save(Product.builder()
                .seller(seller)
                .category(electronics)
                .name("Wireless Noise Canceling Headphones")
                .brand("SoundMax")
                .description("Over-ear Bluetooth headphones with active noise cancelling")
                .basePrice(BigDecimal.valueOf(150.00))
                .mrp(BigDecimal.valueOf(200.00))
                .stockQty(0)
                .isActive(true)
                .slug("wireless-nc-headphones")
                .build());
    }

    @Test
    public void testSearchFallback_TextQuery_FindsMatchingProducts() {
        Page<ProductDocument> results = searchService.searchProducts(
                "UltraBook", null, null, null, null, null, null, null, 0, 10
        );

        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
        assertEquals("UltraBook Pro 15", results.getContent().get(0).getName());
        assertEquals("TechBrand", results.getContent().get(0).getBrand());
    }

    @Test
    public void testSearchFallback_FilterInStock_ReturnsOnlyInStock() {
        Page<ProductDocument> inStockResults = searchService.searchProducts(
                null, null, null, null, null, null, null, true, 0, 10
        );

        assertNotNull(inStockResults);
        assertEquals(1, inStockResults.getTotalElements());
        assertEquals("UltraBook Pro 15", inStockResults.getContent().get(0).getName());

        Page<ProductDocument> outOfStockResults = searchService.searchProducts(
                null, null, null, null, null, null, null, false, 0, 10
        );

        assertNotNull(outOfStockResults);
        assertEquals(1, outOfStockResults.getTotalElements());
        assertEquals("Wireless Noise Canceling Headphones", outOfStockResults.getContent().get(0).getName());
    }

    @Test
    public void testSearchFallback_PriceRangeFilter() {
        Page<ProductDocument> results = searchService.searchProducts(
                null, null, null, 100.0, 500.0, null, null, null, 0, 10
        );

        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
        assertEquals("Wireless Noise Canceling Headphones", results.getContent().get(0).getName());
    }

    @Test
    public void testAutocompleteFallback() {
        List<String> suggestions = searchService.getAutocompleteSuggestions("Ultra");
        assertNotNull(suggestions);
        assertTrue(suggestions.contains("UltraBook Pro 15"));
    }
}
