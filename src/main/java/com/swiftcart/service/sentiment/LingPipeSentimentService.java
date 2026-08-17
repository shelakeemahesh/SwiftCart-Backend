package com.swiftcart.service.sentiment;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.lm.NGramProcessLM;
import com.swiftcart.enums.ReviewSentiment;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class LingPipeSentimentService {

    private static final Logger log = LoggerFactory.getLogger(LingPipeSentimentService.class);

    private static final String[] CATEGORIES = {"POSITIVE", "NEGATIVE", "NEUTRAL"};
    private static final int N_GRAM = 6;

    private DynamicLMClassifier<NGramProcessLM> classifier;

    public record SentimentResult(ReviewSentiment sentiment, double confidenceScore, List<String> aspects) {}

    // Aspect detection pattern map
    private static final Map<String, List<Pattern>> ASPECT_PATTERNS = new LinkedHashMap<>();

    static {
        ASPECT_PATTERNS.put("DEFECTIVE_PRODUCT", List.of(
                Pattern.compile("(?i)\\b(defective|broken|stopped working|doesn't work|not working|malfunction|faulty|dead on arrival|doa)\\b")
        ));
        ASPECT_PATTERNS.put("POOR_QUALITY", List.of(
                Pattern.compile("(?i)\\b(poor quality|cheap material|flimsy|bad quality|terrible build|substandard|feels cheap|scratched)\\b")
        ));
        ASPECT_PATTERNS.put("DELIVERY_ISSUE", List.of(
                Pattern.compile("(?i)\\b(late delivery|delayed|never arrived|package lost|slow shipping|took weeks)\\b")
        ));
        ASPECT_PATTERNS.put("DAMAGED_PACKAGING", List.of(
                Pattern.compile("(?i)\\b(damaged package|torn box|crushed box|opened box|damaged packaging)\\b")
        ));
        ASPECT_PATTERNS.put("WRONG_ITEM", List.of(
                Pattern.compile("(?i)\\b(wrong item|different color|different size|not as pictured|misleading description|fake|counterfeit)\\b")
        ));
        ASPECT_PATTERNS.put("HIGH_QUALITY", List.of(
                Pattern.compile("(?i)\\b(high quality|premium quality|excellent build|durable|sturdy|well made|solid construction)\\b")
        ));
        ASPECT_PATTERNS.put("GREAT_VALUE", List.of(
                Pattern.compile("(?i)\\b(great value|worth the money|good price|bargain|value for money|affordable price)\\b")
        ));
        ASPECT_PATTERNS.put("FAST_SHIPPING", List.of(
                Pattern.compile("(?i)\\b(fast delivery|quick shipping|arrived early|fast shipping|on time delivery)\\b")
        ));
        ASPECT_PATTERNS.put("CUSTOMER_SATISFACTION", List.of(
                Pattern.compile("(?i)\\b(highly recommend|love this|exceeded expectations|very satisfied|awesome product|five stars)\\b")
        ));
    }

    @PostConstruct
    public void init() {
        log.info("Initializing LingPipe Sentiment Classifier (n-gram={})...", N_GRAM);
        classifier = DynamicLMClassifier.createNGramProcess(CATEGORIES, N_GRAM);
        trainInitialCorpus();
        log.info("LingPipe Sentiment Classifier successfully trained and ready.");
    }

    private void trainInitialCorpus() {
        // Positive corpus
        List<String> positiveSamples = List.of(
                "Great product, works perfectly and exceeds expectations!",
                "Amazing quality, fast shipping, very happy with this purchase.",
                "Excellent build quality and battery life. Highly recommended!",
                "Best product in this price range, authentic and premium finish.",
                "Loved it! Super comfortable and exactly what I was looking for.",
                "Outstanding sound quality and deep bass. Worth every penny.",
                "Very satisfied with SwiftCart delivery and product condition.",
                "Five stars! Top notch performance, elegant design and easy setup.",
                "Super happy with the quality! Arrived early and in great packaging.",
                "Awesome item! Everything works as described. Will buy again."
        );

        // Negative corpus
        List<String> negativeSamples = List.of(
                "Terrible experience, product stopped working after two days.",
                "Defective item, arrived broken and damaged. Very disappointed.",
                "Worst purchase ever. Poor quality material and cheap plastic.",
                "Fake and counterfeit item. Does not match the description.",
                "Seller is unresponsive. The item was missing parts and scratched.",
                "Waste of money! Broken on arrival and unusable.",
                "Extremely disappointed. Poor sound, battery doesn't hold charge.",
                "Horrible quality. Broke within 24 hours of usage.",
                "Misleading listing and damaged packaging. Demanding a refund.",
                "Avoid this seller! Received completely different and broken item."
        );

        // Neutral corpus
        List<String> neutralSamples = List.of(
                "The product is okay, average quality for the price.",
                "Decent product, nothing special but does the job.",
                "Standard item. As expected, neither great nor bad.",
                "Normal delivery, acceptable quality. Just okay.",
                "Average performance. It meets basic requirements.",
                "Fair product for this price point. Packaging was plain."
        );

        for (String sample : positiveSamples) {
            train(sample, ReviewSentiment.POSITIVE);
        }
        for (String sample : negativeSamples) {
            train(sample, ReviewSentiment.NEGATIVE);
        }
        for (String sample : neutralSamples) {
            train(sample, ReviewSentiment.NEUTRAL);
        }
    }

    public synchronized void train(String text, ReviewSentiment sentiment) {
        if (text == null || text.isBlank() || sentiment == null || classifier == null) return;
        Classification classification = new Classification(sentiment.name());
        Classified<CharSequence> classified = new Classified<>(text, classification);
        classifier.handle(classified);
    }

    /**
     * Analyze review text, title, and star rating.
     */
    public SentimentResult analyzeReview(String title, String body, int rating) {
        String fullText = ((title != null ? title : "") + " " + (body != null ? body : "")).trim();
        List<String> aspects = extractAspects(fullText);

        if (fullText.isBlank()) {
            ReviewSentiment sentimentFromRating = rating >= 4 ? ReviewSentiment.POSITIVE : (rating <= 2 ? ReviewSentiment.NEGATIVE : ReviewSentiment.NEUTRAL);
            double score = rating >= 4 ? 0.85 : (rating <= 2 ? 0.85 : 0.60);
            return new SentimentResult(sentimentFromRating, score, aspects);
        }

        // LingPipe Joint classification
        JointClassification jc = classifier.classify(fullText);
        String bestCategory = jc.bestCategory();
        double bestProb = 0.5;
        try {
            bestProb = Math.min(1.0, Math.max(0.5, Math.exp(jc.conditionalProbability(0))));
        } catch (Exception ignored) {}

        ReviewSentiment nlpSentiment = ReviewSentiment.valueOf(bestCategory);

        // Hybrid multi-signal scoring (integrating rating + LingPipe NLP)
        ReviewSentiment finalSentiment;
        double finalScore;

        if (rating >= 4) {
            if (nlpSentiment == ReviewSentiment.POSITIVE) {
                finalSentiment = ReviewSentiment.POSITIVE;
                finalScore = Math.min(0.99, bestProb * 1.1);
            } else if (nlpSentiment == ReviewSentiment.NEGATIVE) {
                // Mixed signal (high stars but negative wording)
                finalSentiment = rating == 5 ? ReviewSentiment.POSITIVE : ReviewSentiment.NEUTRAL;
                finalScore = 0.65;
            } else {
                finalSentiment = ReviewSentiment.POSITIVE;
                finalScore = 0.80;
            }
        } else if (rating <= 2) {
            if (nlpSentiment == ReviewSentiment.NEGATIVE) {
                finalSentiment = ReviewSentiment.NEGATIVE;
                finalScore = Math.min(0.99, bestProb * 1.15);
            } else if (nlpSentiment == ReviewSentiment.POSITIVE) {
                finalSentiment = rating == 1 ? ReviewSentiment.NEGATIVE : ReviewSentiment.NEUTRAL;
                finalScore = 0.65;
            } else {
                finalSentiment = ReviewSentiment.NEGATIVE;
                finalScore = 0.85;
            }
        } else { // 3-star rating
            finalSentiment = nlpSentiment;
            finalScore = bestProb;
        }

        return new SentimentResult(finalSentiment, Math.round(finalScore * 100.0) / 100.0, aspects);
    }

    public List<String> extractAspects(String text) {
        if (text == null || text.isBlank()) return List.of();
        List<String> detected = new ArrayList<>();
        for (Map.Entry<String, List<Pattern>> entry : ASPECT_PATTERNS.entrySet()) {
            for (Pattern pattern : entry.getValue()) {
                if (pattern.matcher(text).find()) {
                    detected.add(entry.getKey());
                    break;
                }
            }
        }
        return detected;
    }
}
