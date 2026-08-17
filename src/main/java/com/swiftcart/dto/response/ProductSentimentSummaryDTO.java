package com.swiftcart.dto.response;

import java.util.List;
import java.util.Map;

public class ProductSentimentSummaryDTO {
    private Long productId;
    private String productName;
    private long totalReviews;
    private long positiveCount;
    private long neutralCount;
    private long negativeCount;
    private double positivePercentage;
    private double negativePercentage;
    private double averageSentimentScore;
    private List<String> topAspects;
    private Map<String, Long> aspectCounts;

    public ProductSentimentSummaryDTO() {}

    public ProductSentimentSummaryDTO(Long productId, String productName, long totalReviews, long positiveCount,
                                     long neutralCount, long negativeCount, double positivePercentage,
                                     double negativePercentage, double averageSentimentScore,
                                     List<String> topAspects, Map<String, Long> aspectCounts) {
        this.productId = productId;
        this.productName = productName;
        this.totalReviews = totalReviews;
        this.positiveCount = positiveCount;
        this.neutralCount = neutralCount;
        this.negativeCount = negativeCount;
        this.positivePercentage = positivePercentage;
        this.negativePercentage = negativePercentage;
        this.averageSentimentScore = averageSentimentScore;
        this.topAspects = topAspects;
        this.aspectCounts = aspectCounts;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }

    public long getPositiveCount() { return positiveCount; }
    public void setPositiveCount(long positiveCount) { this.positiveCount = positiveCount; }

    public long getNeutralCount() { return neutralCount; }
    public void setNeutralCount(long neutralCount) { this.neutralCount = neutralCount; }

    public long getNegativeCount() { return negativeCount; }
    public void setNegativeCount(long negativeCount) { this.negativeCount = negativeCount; }

    public double getPositivePercentage() { return positivePercentage; }
    public void setPositivePercentage(double positivePercentage) { this.positivePercentage = positivePercentage; }

    public double getNegativePercentage() { return negativePercentage; }
    public void setNegativePercentage(double negativePercentage) { this.negativePercentage = negativePercentage; }

    public double getAverageSentimentScore() { return averageSentimentScore; }
    public void setAverageSentimentScore(double averageSentimentScore) { this.averageSentimentScore = averageSentimentScore; }

    public List<String> getTopAspects() { return topAspects; }
    public void setTopAspects(List<String> topAspects) { this.topAspects = topAspects; }

    public Map<String, Long> getAspectCounts() { return aspectCounts; }
    public void setAspectCounts(Map<String, Long> aspectCounts) { this.aspectCounts = aspectCounts; }
}
