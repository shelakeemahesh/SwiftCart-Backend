package com.swiftcart.service.ai;

import com.swiftcart.dto.response.AiChatResponseDTO;
import com.swiftcart.dto.response.ProductRecommendationDTO;
import com.swiftcart.entity.Category;
import com.swiftcart.entity.Product;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductRagServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private VectorStore vectorStore;
    private ProductVectorSyncService syncService;
    private ProductRagService ragService;

    @BeforeEach
    void setUp() {
        SemanticCatalogEmbeddingModel embeddingModel = new SemanticCatalogEmbeddingModel();
        vectorStore = new SimpleVectorStore(embeddingModel);
        syncService = new ProductVectorSyncService(productRepository, reviewRepository, vectorStore);
        ragService = new ProductRagService(vectorStore, productRepository, syncService);
        org.springframework.test.util.ReflectionTestUtils.setField(ragService, "defaultTopK", 4);
    }

    @Test
    @DisplayName("Should index products and find relevant recommendations through vector search")
    void testVectorSearchAndRagResponse() {
        Category electronics = new Category();
        electronics.setName("Electronics");

        Product laptop = new Product();
        laptop.setId(1L);
        laptop.setName("SwiftPro Ultra Laptop 16GB RAM");
        laptop.setBrand("SwiftTech");
        laptop.setCategory(electronics);
        laptop.setBasePrice(BigDecimal.valueOf(1199.99));
        laptop.setStockQty(15);
        laptop.setAverageRating(BigDecimal.valueOf(4.8));
        laptop.setReviewCount(42);
        laptop.setDescription("High-performance laptop featuring 16GB DDR5 RAM and blazing fast M.2 NVMe SSD storage.");

        Product headphones = new Product();
        headphones.setId(2L);
        headphones.setName("SwiftSound Active Noise Cancelling Headphones");
        headphones.setBrand("SwiftSound");
        headphones.setCategory(electronics);
        headphones.setBasePrice(BigDecimal.valueOf(199.99));
        headphones.setStockQty(30);
        headphones.setAverageRating(BigDecimal.valueOf(4.9));
        headphones.setReviewCount(88);
        headphones.setDescription("Premium wireless headphones with 40-hour battery life and superior active noise cancellation.");

        // Index products into vector store
        syncService.indexProduct(laptop);
        syncService.indexProduct(headphones);

        // Search for laptop
        List<Document> laptopResults = ragService.searchRelevantDocuments("laptop with 16GB RAM", 3);
        assertFalse(laptopResults.isEmpty());
        assertTrue(laptopResults.get(0).getContent().contains("Laptop"));

        // RAG query for headphones
        AiChatResponseDTO answer = ragService.answerProductQuestion("Tell me about noise cancelling headphones", null);
        assertNotNull(answer);
        assertEquals("rag_recommendation", answer.getType());
        assertFalse(answer.getRecommendedProducts().isEmpty());
        assertTrue(answer.getReply().contains("SwiftSound") || answer.getReply().contains("headphones"));
    }
}
