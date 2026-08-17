package com.swiftcart.dto.response;

import java.util.List;
import java.util.Map;

public class PlatformSentimentOverviewDTO {
    private long totalReviewsAnalyzed;
    private long positiveReviews;
    private long neutralReviews;
    private long negativeReviews;
    private double positiveRatio;
    private double negativeRatio;
    private long atRiskVendorsCount;
    private List<VendorRiskDTO> highRiskVendors;
    private Map<String, Long> topNegativeAspects;

    public PlatformSentimentOverviewDTO() {}

    public PlatformSentimentOverviewDTO(long totalReviewsAnalyzed, long positiveReviews, long neutralReviews,
                                       long negativeReviews, double positiveRatio, double negativeRatio,
                                       long atRiskVendorsCount, List<VendorRiskDTO> highRiskVendors,
                                       Map<String, Long> topNegativeAspects) {
        this.totalReviewsAnalyzed = totalReviewsAnalyzed;
        this.positiveReviews = positiveReviews;
        this.neutralReviews = neutralReviews;
        this.negativeReviews = negativeReviews;
        this.positiveRatio = positiveRatio;
        this.negativeRatio = negativeRatio;
        this.atRiskVendorsCount = atRiskVendorsCount;
        this.highRiskVendors = highRiskVendors;
        this.topNegativeAspects = topNegativeAspects;
    }

    public long getTotalReviewsAnalyzed() { return totalReviewsAnalyzed; }
    public void setTotalReviewsAnalyzed(long totalReviewsAnalyzed) { this.totalReviewsAnalyzed = totalReviewsAnalyzed; }

    public long getPositiveReviews() { return positiveReviews; }
    public void setPositiveReviews(long positiveReviews) { this.positiveReviews = positiveReviews; }

    public long getNeutralReviews() { return neutralReviews; }
    public void setNeutralReviews(long neutralReviews) { this.neutralReviews = neutralReviews; }

    public long getNegativeReviews() { return negativeReviews; }
    public void setNegativeReviews(long negativeReviews) { this.negativeReviews = negativeReviews; }

    public double getPositiveRatio() { return positiveRatio; }
    public void setPositiveRatio(double positiveRatio) { this.positiveRatio = positiveRatio; }

    public double getNegativeRatio() { return negativeRatio; }
    public void setNegativeRatio(double negativeRatio) { this.negativeRatio = negativeRatio; }

    public long getAtRiskVendorsCount() { return atRiskVendorsCount; }
    public void setAtRiskVendorsCount(long atRiskVendorsCount) { this.atRiskVendorsCount = atRiskVendorsCount; }

    public List<VendorRiskDTO> getHighRiskVendors() { return highRiskVendors; }
    public void setHighRiskVendors(List<VendorRiskDTO> highRiskVendors) { this.highRiskVendors = highRiskVendors; }

    public Map<String, Long> getTopNegativeAspects() { return topNegativeAspects; }
    public void setTopNegativeAspects(Map<String, Long> topNegativeAspects) { this.topNegativeAspects = topNegativeAspects; }
}
