package com.swiftcart.dto.response;

import java.util.List;

public class VendorRiskDTO {
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;
    private long totalReviews;
    private long negativeReviews;
    private double negativeReviewRate;
    private String riskLevel; // CRITICAL, HIGH, MODERATE, LOW
    private List<String> recurringIssues;
    private List<String> affectedProductNames;

    public VendorRiskDTO() {}

    public VendorRiskDTO(Long sellerId, String sellerName, String sellerEmail, long totalReviews,
                         long negativeReviews, double negativeReviewRate, String riskLevel,
                         List<String> recurringIssues, List<String> affectedProductNames) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerEmail = sellerEmail;
        this.totalReviews = totalReviews;
        this.negativeReviews = negativeReviews;
        this.negativeReviewRate = negativeReviewRate;
        this.riskLevel = riskLevel;
        this.recurringIssues = recurringIssues;
        this.affectedProductNames = affectedProductNames;
    }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }

    public long getNegativeReviews() { return negativeReviews; }
    public void setNegativeReviews(long negativeReviews) { this.negativeReviews = negativeReviews; }

    public double getNegativeReviewRate() { return negativeReviewRate; }
    public void setNegativeReviewRate(double negativeReviewRate) { this.negativeReviewRate = negativeReviewRate; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public List<String> getRecurringIssues() { return recurringIssues; }
    public void setRecurringIssues(List<String> recurringIssues) { this.recurringIssues = recurringIssues; }

    public List<String> getAffectedProductNames() { return affectedProductNames; }
    public void setAffectedProductNames(List<String> affectedProductNames) { this.affectedProductNames = affectedProductNames; }
}
