package com.swiftcart.service.ai;

import com.swiftcart.dto.response.AiChatResponseDTO;
import com.swiftcart.dto.response.ProductRecommendationDTO;
import com.swiftcart.entity.Product;
import com.swiftcart.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductRagService {

    private static final Logger log = LoggerFactory.getLogger(ProductRagService.class);

    private final VectorStore vectorStore;
    private final ProductRepository productRepository;
    private final ProductVectorSyncService productVectorSyncService;

    @Value("${app.ai.rag.top-k:4}")
    private int defaultTopK = 4;

    public ProductRagService(
            VectorStore vectorStore,
            ProductRepository productRepository,
            ProductVectorSyncService productVectorSyncService) {
        this.vectorStore = vectorStore;
        this.productRepository = productRepository;
        this.productVectorSyncService = productVectorSyncService;
    }

    /**
     * Retrieves top-K semantically relevant products from the VectorStore.
     */
    public List<Document> searchRelevantDocuments(String query, int topK) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        try {
            int effectiveTopK = topK > 0 ? topK : (defaultTopK > 0 ? defaultTopK : 4);
            SearchRequest request = SearchRequest.query(query)
                    .withTopK(effectiveTopK)
                    .withSimilarityThreshold(SearchRequest.SIMILARITY_THRESHOLD_ACCEPT_ALL);

            List<Document> results = vectorStore.similaritySearch(request);
            if (results == null || results.isEmpty()) {
                // If vector store is empty, trigger on-the-fly sync and retry
                log.info("VectorStore returned 0 results. Triggering catalog sync...");
                productVectorSyncService.syncAllProducts();
                results = vectorStore.similaritySearch(request);
            }
            return results != null ? results : List.of();
        } catch (Exception e) {
            log.warn("Vector similarity search failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Transforms retrieved documents into UI product recommendation DTOs.
     */
    public List<ProductRecommendationDTO> toRecommendations(List<Document> documents) {
        if (documents == null || documents.isEmpty()) return List.of();

        List<ProductRecommendationDTO> recommendations = new ArrayList<>();
        for (Document doc : documents) {
            Map<String, Object> meta = doc.getMetadata();
            if (meta == null) continue;

            Object idObj = meta.get("productId");
            Long id = null;
            if (idObj instanceof Number num) id = num.longValue();
            else if (idObj instanceof String str && !str.isBlank()) id = Long.valueOf(str);

            String name = (String) meta.getOrDefault("name", "");
            String slug = (String) meta.getOrDefault("slug", "");
            String brand = (String) meta.getOrDefault("brand", "");
            String category = (String) meta.getOrDefault("category", "");

            double priceVal = meta.containsKey("price") ? ((Number) meta.get("price")).doubleValue() : 0.0;
            double mrpVal = meta.containsKey("mrp") ? ((Number) meta.get("mrp")).doubleValue() : priceVal;
            double ratingVal = meta.containsKey("averageRating") ? ((Number) meta.get("averageRating")).doubleValue() : 0.0;
            int reviewCount = meta.containsKey("reviewCount") ? ((Number) meta.get("reviewCount")).intValue() : 0;
            boolean inStock = meta.containsKey("inStock") ? (Boolean) meta.get("inStock") : true;
            String imageUrl = (String) meta.getOrDefault("imageUrl", "");
            String highlight = (String) meta.getOrDefault("highlight", "");

            recommendations.add(new ProductRecommendationDTO(
                    id, name, slug, brand, category,
                    BigDecimal.valueOf(priceVal),
                    BigDecimal.valueOf(mrpVal),
                    BigDecimal.valueOf(ratingVal),
                    reviewCount, inStock, imageUrl, highlight
            ));
        }
        return recommendations;
    }

    /**
     * Executes the RAG generation pipeline for product inquiries.
     */
    public AiChatResponseDTO answerProductQuestion(String userQuery, Long targetProductId) {
        // If query is for a specific product ID
        if (targetProductId != null) {
            Optional<Product> productOpt = productRepository.findById(targetProductId);
            if (productOpt.isPresent()) {
                Product p = productOpt.get();
                Document doc = productVectorSyncService.toDocument(p);
                List<ProductRecommendationDTO> singleRec = toRecommendations(List.of(doc));
                String reply = synthesizeSpecificProductAnswer(p, userQuery);
                return AiChatResponseDTO.rag(reply, singleRec, List.of("🚚 Track my order", "↩️ Return / Refund", "🗣️ Talk to human"));
            }
        }

        // Vector similarity search across catalog
        List<Document> relevantDocs = searchRelevantDocuments(userQuery, defaultTopK);
        List<ProductRecommendationDTO> recommendations = toRecommendations(relevantDocs);

        if (recommendations.isEmpty()) {
            String fallback = "I searched our product catalog, but couldn't find exact matches for \"" + userQuery +
                    "\". Would you like me to connect you with our customer support team or help you explore all catalog products?";
            return AiChatResponseDTO.text(fallback, List.of("🚚 Track my order", "↩️ Return / Refund", "🗣️ Talk to human"), "/search");
        }

        String reply = synthesizeCatalogAnswer(userQuery, recommendations, relevantDocs);
        List<String> options = List.of("🚚 Track my order", "↩️ Return / Refund", "🗣️ Talk to human");

        return AiChatResponseDTO.rag(reply, recommendations, options);
    }

    private String synthesizeSpecificProductAnswer(Product p, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the details for **").append(p.getName()).append("**:\n\n");
        sb.append("• **Price**: ₹").append(p.getBasePrice());
        if (p.getMrp() != null && p.getMrp().compareTo(p.getBasePrice()) > 0) {
            sb.append(" (Save with original MRP: ₹").append(p.getMrp()).append(")");
        }
        sb.append("\n• **Stock**: ").append(p.getStockQty() > 0 ? (p.getStockQty() + " items available in stock") : "Currently out of stock");
        sb.append("\n• **Customer Rating**: ").append(p.getAverageRating()).append(" / 5 stars (").append(p.getReviewCount()).append(" reviews)");

        if (p.getSpecifications() != null && !p.getSpecifications().isEmpty()) {
            sb.append("\n\n**Key Specs**:");
            p.getSpecifications().forEach((k, v) -> sb.append("\n- ").append(k).append(": ").append(v));
        }

        if (p.getHighlights() != null && !p.getHighlights().isEmpty()) {
            sb.append("\n\n**Highlights**:\n");
            p.getHighlights().forEach(h -> sb.append("- ").append(h).append("\n"));
        }

        return sb.toString();
    }

    private String synthesizeCatalogAnswer(String query, List<ProductRecommendationDTO> recs, List<Document> docs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Based on our product catalog, here are the best matches for your search:\n\n");

        for (int i = 0; i < Math.min(recs.size(), 3); i++) {
            ProductRecommendationDTO p = recs.get(i);
            sb.append(i + 1).append(". **").append(p.getName()).append("**");
            if (p.getBrand() != null && !p.getBrand().isBlank()) {
                sb.append(" by ").append(p.getBrand());
            }
            sb.append(" — **₹").append(p.getPrice()).append("**");
            if (p.getAverageRating() != null && p.getAverageRating().doubleValue() > 0) {
                sb.append(" (⭐ ").append(p.getAverageRating()).append("/5)");
            }
            sb.append("\n   Status: ").append(p.isInStock() ? "✅ In Stock" : "❌ Out of Stock");
            if (p.getHighlight() != null && !p.getHighlight().isBlank()) {
                sb.append(" | ").append(p.getHighlight());
            }
            sb.append("\n\n");
        }

        sb.append("You can click on any product card below to view detailed specifications or purchase!");
        return sb.toString();
    }
}
