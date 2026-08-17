package com.swiftcart.config;

import com.swiftcart.service.ai.SemanticCatalogEmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringAiConfig {

    private static final Logger log = LoggerFactory.getLogger(SpringAiConfig.class);

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(SemanticCatalogEmbeddingModel semanticCatalogEmbeddingModel) {
        log.info("Configuring Semantic Catalog EmbeddingModel for Spring AI...");
        return semanticCatalogEmbeddingModel;
    }

    @Bean
    @Primary
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        log.info("Initializing Spring AI VectorStore with semantic embeddings...");
        return new SimpleVectorStore(embeddingModel);
    }
}
