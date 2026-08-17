package com.swiftcart.service.sentiment;

import com.swiftcart.enums.ReviewSentiment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LingPipeSentimentServiceTest {

    private LingPipeSentimentService sentimentService;

    @BeforeEach
    void setUp() {
        sentimentService = new LingPipeSentimentService();
        sentimentService.init();
    }

    @Test
    @DisplayName("Should classify positive review with high rating accurately")
    void testPositiveReview() {
        LingPipeSentimentService.SentimentResult result = sentimentService.analyzeReview(
                "Superb product!",
                "Amazing quality, fast shipping, works perfectly and exceeds expectations. Very happy!",
                5
        );

        assertNotNull(result);
        assertEquals(ReviewSentiment.POSITIVE, result.sentiment());
        assertTrue(result.confidenceScore() >= 0.70, "Confidence score should be >= 0.70 for clear positive review");
        assertTrue(result.aspects().contains("HIGH_QUALITY") || result.aspects().contains("FAST_SHIPPING") || result.aspects().contains("CUSTOMER_SATISFACTION"),
                "Should extract positive aspect tags");
    }

    @Test
    @DisplayName("Should classify negative review with low rating accurately and extract defect aspects")
    void testNegativeReviewWithDefect() {
        LingPipeSentimentService.SentimentResult result = sentimentService.analyzeReview(
                "Broken on arrival",
                "Defective product, stopped working after 2 hours. Poor quality and damaged package. Waste of money!",
                1
        );

        assertNotNull(result);
        assertEquals(ReviewSentiment.NEGATIVE, result.sentiment());
        assertTrue(result.confidenceScore() >= 0.75, "Confidence score should be >= 0.75 for clear negative review");
        assertTrue(result.aspects().contains("DEFECTIVE_PRODUCT"), "Should detect DEFECTIVE_PRODUCT aspect");
        assertTrue(result.aspects().contains("POOR_QUALITY") || result.aspects().contains("DAMAGED_PACKAGING"), "Should detect poor quality or damaged packaging aspect");
    }

    @Test
    @DisplayName("Should extract specific aspect tags correctly")
    void testExtractAspects() {
        String reviewText = "Late delivery and damaged packaging, but great value for the price.";
        List<String> aspects = sentimentService.extractAspects(reviewText);

        assertTrue(aspects.contains("DELIVERY_ISSUE"));
        assertTrue(aspects.contains("DAMAGED_PACKAGING"));
        assertTrue(aspects.contains("GREAT_VALUE"));
    }

    @Test
    @DisplayName("Should support dynamic incremental training")
    void testDynamicTraining() {
        sentimentService.train("Super swift experience absolutely remarkable", ReviewSentiment.POSITIVE);
        sentimentService.train("Total scam complete garbage useless", ReviewSentiment.NEGATIVE);

        LingPipeSentimentService.SentimentResult res = sentimentService.analyzeReview(
                "Scam alert",
                "Total scam complete garbage useless item",
                1
        );

        assertEquals(ReviewSentiment.NEGATIVE, res.sentiment());
    }
}
