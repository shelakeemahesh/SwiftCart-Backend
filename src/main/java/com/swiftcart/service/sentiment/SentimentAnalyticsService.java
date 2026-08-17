package com.swiftcart.service.sentiment;

import com.swiftcart.dto.response.PlatformSentimentOverviewDTO;
import com.swiftcart.dto.response.ProductSentimentSummaryDTO;
import com.swiftcart.dto.response.VendorRiskDTO;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.Review;
import com.swiftcart.entity.User;
import com.swiftcart.enums.ReviewSentiment;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.ReviewRepository;
import com.swiftcart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SentimentAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(SentimentAnalyticsService.class);

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LingPipeSentimentService lingPipeSentimentService;

    @Value("${app.ai.sentiment.vendor-risk-threshold:0.25}")
    private double vendorRiskThreshold;

    @Value("${app.ai.sentiment.min-reviews-for-risk:3}")
    private int minReviewsForRisk;

    public SentimentAnalyticsService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            LingPipeSentimentService lingPipeSentimentService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.lingPipeSentimentService = lingPipeSentimentService;
    }

    /**
     * Get sentiment breakdown and aspect analysis for a specific product.
     */
    public ProductSentimentSummaryDTO getProductSentimentSummary(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (reviews.isEmpty()) {
            return new ProductSentimentSummaryDTO(
                    productId,
                    product.getName(),
                    0, 0, 0, 0, 0.0, 0.0, 0.0,
                    List.of(),
                    Map.of()
            );
        }

        long positiveCount = reviews.stream().filter(r -> r.getSentiment() == ReviewSentiment.POSITIVE).count();
        long neutralCount = reviews.stream().filter(r -> r.getSentiment() == ReviewSentiment.NEUTRAL).count();
        long negativeCount = reviews.stream().filter(r -> r.getSentiment() == ReviewSentiment.NEGATIVE).count();

        double positivePct = Math.round(((double) positiveCount / reviews.size()) * 1000.0) / 10.0;
        double negativePct = Math.round(((double) negativeCount / reviews.size()) * 1000.0) / 10.0;

        double avgScore = reviews.stream()
                .filter(r -> r.getSentimentScore() != null)
                .mapToDouble(Review::getSentimentScore)
                .average()
                .orElse(0.0);
        avgScore = Math.round(avgScore * 100.0) / 100.0;

        // Aggregate aspects
        Map<String, Long> aspectCounts = new LinkedHashMap<>();
        for (Review review : reviews) {
            if (review.getDetectedAspects() != null) {
                for (String aspect : review.getDetectedAspects()) {
                    aspectCounts.put(aspect, aspectCounts.getOrDefault(aspect, 0L) + 1);
                }
            }
        }

        List<String> topAspects = aspectCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return new ProductSentimentSummaryDTO(
                productId,
                product.getName(),
                reviews.size(),
                positiveCount,
                neutralCount,
                negativeCount,
                positivePct,
                negativePct,
                avgScore,
                topAspects,
                aspectCounts
        );
    }

    /**
     * Get vendor / seller risk insights based on reviews across all seller products.
     */
    public VendorRiskDTO getSellerSentimentInsights(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found with id: " + sellerId));

        List<Review> sellerReviews = reviewRepository.findByProductSellerId(sellerId);
        long totalReviews = sellerReviews.size();
        long negativeReviews = sellerReviews.stream().filter(r -> r.getSentiment() == ReviewSentiment.NEGATIVE).count();

        double negativeRate = totalReviews > 0 ? (double) negativeReviews / totalReviews : 0.0;
        negativeRate = Math.round(negativeRate * 100.0) / 100.0;

        String riskLevel;
        if (totalReviews >= minReviewsForRisk && negativeRate >= 0.50) {
            riskLevel = "CRITICAL";
        } else if (totalReviews >= minReviewsForRisk && negativeRate >= vendorRiskThreshold) {
            riskLevel = "HIGH";
        } else if (totalReviews >= minReviewsForRisk && negativeRate >= 0.15) {
            riskLevel = "MODERATE";
        } else {
            riskLevel = "LOW";
        }

        // Collect recurring negative issues
        Map<String, Long> negativeAspects = new HashMap<>();
        Set<String> affectedProducts = new LinkedHashSet<>();

        for (Review r : sellerReviews) {
            if (r.getSentiment() == ReviewSentiment.NEGATIVE) {
                if (r.getProduct() != null) {
                    affectedProducts.add(r.getProduct().getName());
                }
                if (r.getDetectedAspects() != null) {
                    for (String aspect : r.getDetectedAspects()) {
                        negativeAspects.put(aspect, negativeAspects.getOrDefault(aspect, 0L) + 1);
                    }
                }
            }
        }

        List<String> recurringIssues = negativeAspects.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        String sellerName = seller.getBusinessName() != null && !seller.getBusinessName().isBlank()
                ? seller.getBusinessName()
                : (seller.getName() != null && !seller.getName().isBlank() ? seller.getName() : seller.getEmail());

        return new VendorRiskDTO(
                sellerId,
                sellerName,
                seller.getEmail(),
                totalReviews,
                negativeReviews,
                negativeRate,
                riskLevel,
                recurringIssues,
                new ArrayList<>(affectedProducts)
        );
    }

    /**
     * Get platform-wide sentiment analytics overview and detect high-risk vendors for Admin dashboard.
     */
    public PlatformSentimentOverviewDTO getPlatformSentimentOverview() {
        List<Review> allReviews = reviewRepository.findAll();
        long total = allReviews.size();
        if (total == 0) {
            return new PlatformSentimentOverviewDTO(0, 0, 0, 0, 0.0, 0.0, 0, List.of(), Map.of());
        }

        long positive = allReviews.stream().filter(r -> r.getSentiment() == ReviewSentiment.POSITIVE).count();
        long neutral = allReviews.stream().filter(r -> r.getSentiment() == ReviewSentiment.NEUTRAL).count();
        long negative = allReviews.stream().filter(r -> r.getSentiment() == ReviewSentiment.NEGATIVE).count();

        double posRatio = Math.round(((double) positive / total) * 100.0) / 100.0;
        double negRatio = Math.round(((double) negative / total) * 100.0) / 100.0;

        // Group reviews by seller
        Map<Long, List<Review>> reviewsBySeller = allReviews.stream()
                .filter(r -> r.getProduct() != null && r.getProduct().getSeller() != null)
                .collect(Collectors.groupingBy(r -> r.getProduct().getSeller().getId()));

        List<VendorRiskDTO> highRiskVendors = new ArrayList<>();
        for (Map.Entry<Long, List<Review>> entry : reviewsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<Review> sellerReviews = entry.getValue();
            if (sellerReviews.size() >= minReviewsForRisk) {
                long sellerNeg = sellerReviews.stream().filter(r -> r.getSentiment() == ReviewSentiment.NEGATIVE).count();
                double rate = (double) sellerNeg / sellerReviews.size();
                if (rate >= vendorRiskThreshold) {
                    highRiskVendors.add(getSellerSentimentInsights(sellerId));
                }
            }
        }

        // Sort high risk vendors descending by negative rate
        highRiskVendors.sort(Comparator.comparingDouble(VendorRiskDTO::getNegativeReviewRate).reversed());

        // Platform-wide negative aspects
        Map<String, Long> topNegativeAspects = new LinkedHashMap<>();
        for (Review r : allReviews) {
            if (r.getSentiment() == ReviewSentiment.NEGATIVE && r.getDetectedAspects() != null) {
                for (String aspect : r.getDetectedAspects()) {
                    topNegativeAspects.put(aspect, topNegativeAspects.getOrDefault(aspect, 0L) + 1);
                }
            }
        }

        return new PlatformSentimentOverviewDTO(
                total,
                positive,
                neutral,
                negative,
                posRatio,
                negRatio,
                highRiskVendors.size(),
                highRiskVendors,
                topNegativeAspects
        );
    }

    /**
     * Batch analyze existing or unclassified reviews across the system.
     */
    @Transactional
    public Map<String, Object> batchAnalyzeReviews(boolean forceReanalyzeAll) {
        List<Review> targetReviews = forceReanalyzeAll
                ? reviewRepository.findAll()
                : reviewRepository.findBySentimentIsNull();

        int analyzedCount = 0;
        for (Review review : targetReviews) {
            LingPipeSentimentService.SentimentResult result = lingPipeSentimentService.analyzeReview(
                    review.getTitle(),
                    review.getBody(),
                    review.getRating()
            );

            review.setSentiment(result.sentiment());
            review.setSentimentScore(result.confidenceScore());
            review.setDetectedAspects(result.aspects());
            reviewRepository.save(review);
            analyzedCount++;
        }

        log.info("Batch sentiment analysis completed for {} reviews.", analyzedCount);
        return Map.of(
                "totalAnalyzed", analyzedCount,
                "status", "SUCCESS",
                "message", "Successfully analyzed sentiment for " + analyzedCount + " customer reviews."
        );
    }
}
