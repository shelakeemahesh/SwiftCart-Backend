package com.swiftcart.service.sentiment;

import com.swiftcart.dto.response.PlatformSentimentOverviewDTO;
import com.swiftcart.dto.response.ProductSentimentSummaryDTO;
import com.swiftcart.dto.response.VendorRiskDTO;
import com.swiftcart.entity.Category;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.Review;
import com.swiftcart.entity.User;
import com.swiftcart.enums.ReviewSentiment;
import com.swiftcart.enums.Role;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.ReviewRepository;
import com.swiftcart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SentimentAnalyticsServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LingPipeSentimentService lingPipeSentimentService;

    private SentimentAnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new SentimentAnalyticsService(
                reviewRepository,
                productRepository,
                userRepository,
                lingPipeSentimentService
        );
        ReflectionTestUtils.setField(analyticsService, "vendorRiskThreshold", 0.25);
        ReflectionTestUtils.setField(analyticsService, "minReviewsForRisk", 2);
    }

    @Test
    @DisplayName("Should compute accurate product sentiment breakdown")
    void testGetProductSentimentSummary() {
        Long productId = 101L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Wireless Noise-Cancelling Headphones");

        Review r1 = Review.builder().product(product).sentiment(ReviewSentiment.POSITIVE).sentimentScore(0.92).detectedAspects(List.of("HIGH_QUALITY", "FAST_SHIPPING")).build();
        Review r2 = Review.builder().product(product).sentiment(ReviewSentiment.POSITIVE).sentimentScore(0.88).detectedAspects(List.of("HIGH_QUALITY")).build();
        Review r3 = Review.builder().product(product).sentiment(ReviewSentiment.NEGATIVE).sentimentScore(0.80).detectedAspects(List.of("DELIVERY_ISSUE")).build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductId(productId)).thenReturn(List.of(r1, r2, r3));

        ProductSentimentSummaryDTO summary = analyticsService.getProductSentimentSummary(productId);

        assertNotNull(summary);
        assertEquals(3, summary.getTotalReviews());
        assertEquals(2, summary.getPositiveCount());
        assertEquals(1, summary.getNegativeCount());
        assertEquals(0, summary.getNeutralCount());
        assertEquals(66.7, summary.getPositivePercentage());
        assertEquals(33.3, summary.getNegativePercentage());
        assertTrue(summary.getTopAspects().contains("HIGH_QUALITY"));
    }

    @Test
    @DisplayName("Should detect high risk vendors with excessive negative reviews")
    void testGetSellerSentimentInsights() {
        Long sellerId = 55L;
        User seller = new User();
        seller.setId(sellerId);
        seller.setName("Acme Electronics");
        seller.setEmail("seller@acme.com");

        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Faulty Charger");
        p1.setSeller(seller);

        Review badRev1 = Review.builder().product(p1).sentiment(ReviewSentiment.NEGATIVE).detectedAspects(List.of("DEFECTIVE_PRODUCT")).build();
        Review badRev2 = Review.builder().product(p1).sentiment(ReviewSentiment.NEGATIVE).detectedAspects(List.of("DEFECTIVE_PRODUCT", "POOR_QUALITY")).build();
        Review goodRev = Review.builder().product(p1).sentiment(ReviewSentiment.POSITIVE).detectedAspects(List.of("FAST_SHIPPING")).build();

        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(reviewRepository.findByProductSellerId(sellerId)).thenReturn(List.of(badRev1, badRev2, goodRev));

        VendorRiskDTO risk = analyticsService.getSellerSentimentInsights(sellerId);

        assertNotNull(risk);
        assertEquals(3, risk.getTotalReviews());
        assertEquals(2, risk.getNegativeReviews());
        assertEquals(0.67, risk.getNegativeReviewRate());
        assertEquals("CRITICAL", risk.getRiskLevel());
        assertTrue(risk.getRecurringIssues().contains("DEFECTIVE_PRODUCT"));
        assertTrue(risk.getAffectedProductNames().contains("Faulty Charger"));
    }
}
