package com.swiftcart.service.ai;

import com.swiftcart.entity.Product;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProductVectorSyncService {

    private static final Logger log = LoggerFactory.getLogger(ProductVectorSyncService.class);

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final VectorStore vectorStore;

    public ProductVectorSyncService(
            ProductRepository productRepository,
            ReviewRepository reviewRepository,
            VectorStore vectorStore) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.vectorStore = vectorStore;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application started. Synchronizing product catalog into Spring AI VectorStore...");
        try {
            int count = syncAllProducts();
            log.info("Successfully indexed {} products into Spring AI VectorStore.", count);
        } catch (Exception e) {
            log.warn("Failed initial vector store sync: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public int syncAllProducts() {
        List<Product> products = productRepository.findAllForVectorIndexing();
        if (products.isEmpty()) {
            log.info("No products found to index in vector store.");
            return 0;
        }

        List<Document> documents = new ArrayList<>();
        for (Product p : products) {
            try {
                documents.add(toDocument(p));
            } catch (Exception ex) {
                log.warn("Error converting product id={} to Document: {}", p.getId(), ex.getMessage());
            }
        }

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
        return documents.size();
    }

    public void indexProduct(Product product) {
        if (product == null) return;
        Document doc = toDocument(product);
        vectorStore.add(List.of(doc));
        log.info("Indexed product [{}] (id={}) into Spring AI VectorStore", product.getName(), product.getId());
    }

    public Document toDocument(Product product) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("productId", product.getId());
        metadata.put("name", product.getName() != null ? product.getName() : "");
        metadata.put("slug", product.getSlug() != null ? product.getSlug() : "");
        metadata.put("brand", product.getBrand() != null ? product.getBrand() : "");

        String categoryName = "";
        try {
            if (product.getCategory() != null) {
                categoryName = product.getCategory().getName();
            }
        } catch (Exception ignored) {}
        metadata.put("category", categoryName);

        metadata.put("price", product.getBasePrice() != null ? product.getBasePrice().doubleValue() : 0.0);
        metadata.put("mrp", product.getMrp() != null ? product.getMrp().doubleValue() : 0.0);
        metadata.put("averageRating", product.getAverageRating() != null ? product.getAverageRating().doubleValue() : 0.0);
        metadata.put("reviewCount", product.getReviewCount());
        metadata.put("inStock", product.getStockQty() > 0 && product.isActive());
        metadata.put("stockQty", product.getStockQty());

        String imageUrl = "";
        try {
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                imageUrl = product.getImages().get(0).getImageUrl();
            }
        } catch (Exception ignored) {}
        metadata.put("imageUrl", imageUrl);

        String firstHighlight = "";
        try {
            if (product.getHighlights() != null && !product.getHighlights().isEmpty()) {
                firstHighlight = product.getHighlights().get(0);
            }
        } catch (Exception ignored) {}
        metadata.put("highlight", firstHighlight);

        // Build rich textual content for RAG embedding
        StringBuilder content = new StringBuilder();
        content.append("Product Name: ").append(product.getName()).append("\n");
        if (product.getBrand() != null) content.append("Brand: ").append(product.getBrand()).append("\n");
        if (!categoryName.isBlank()) content.append("Category: ").append(categoryName).append("\n");
        if (product.getBasePrice() != null) content.append("Price: ₹").append(product.getBasePrice()).append("\n");
        if (product.getMrp() != null) content.append("Original MRP: ₹").append(product.getMrp()).append("\n");
        content.append("Stock Status: ").append(product.getStockQty() > 0 ? (product.getStockQty() + " items available in stock") : "Out of stock").append("\n");
        if (product.getAverageRating() != null) content.append("Customer Rating: ").append(product.getAverageRating()).append(" / 5 stars (").append(product.getReviewCount()).append(" reviews)\n");

        try {
            if (product.getHighlights() != null && !product.getHighlights().isEmpty()) {
                content.append("Highlights: ").append(String.join("; ", product.getHighlights())).append("\n");
            }
        } catch (Exception ignored) {}

        try {
            if (product.getSpecifications() != null && !product.getSpecifications().isEmpty()) {
                content.append("Specifications: ");
                product.getSpecifications().forEach((k, v) -> content.append(k).append("=").append(v).append(", "));
                content.append("\n");
            }
        } catch (Exception ignored) {}

        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            content.append("Description: ").append(product.getDescription()).append("\n");
        }

        String docId = "product_" + product.getId();
        return new Document(docId, content.toString(), metadata);
    }
}
