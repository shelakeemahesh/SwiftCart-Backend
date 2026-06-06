package com.swiftcart.service;

import com.swiftcart.entity.Product;
import com.swiftcart.entity.ProductDocument;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.ProductSearchRepository;
import co.elastic.clients.json.JsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final ProductSearchRepository searchRepository;
    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public SearchService(
            java.util.Optional<ProductSearchRepository> searchRepository,
            ProductRepository productRepository,
            java.util.Optional<ElasticsearchOperations> elasticsearchOperations) {
        this.searchRepository = searchRepository.orElse(null);
        this.productRepository = productRepository;
        this.elasticsearchOperations = elasticsearchOperations.orElse(null);
    }

    @Transactional
    public void indexProduct(Long productId) {
        if (searchRepository == null) {
            log.warn("Elasticsearch is not available, skipping indexing for product ID: {}", productId);
            return;
        }
        productRepository.findByIdWithDetails(productId).ifPresent(product -> {
            ProductDocument doc = ProductDocument.builder()
                    .id(String.valueOf(product.getId()))
                    .name(product.getName())
                    .brand(product.getBrand())
                    .categoryPath(product.getCategory() != null ? product.getCategory().getName() : "")
                    .description(product.getDescription())
                    .price(product.getBasePrice().doubleValue())
                    .rating(product.getAverageRating().doubleValue())
                    .discount(product.getCalculatedDiscountPercent().doubleValue())
                    .inStock(product.getStockQty() > 0)
                    .slug(product.getSlug())
                    .mrp(product.getMrp() != null ? product.getMrp().doubleValue() : 0.0)
                    .reviewCount(product.getReviewCount())
                    .images(product.getImages() != null ? product.getImages().stream().map(com.swiftcart.entity.ProductImage::getImageUrl).collect(Collectors.toList()) : Collections.emptyList())
                    .tags(Collections.singletonList(product.getBrand()))
                    .build();

            searchRepository.save(doc);
            log.info("Indexed product ID: {} successfully in Elasticsearch", productId);
        });
    }

    public Page<ProductDocument> searchProducts(
            String textQuery, String brand, String categoryPath,
            Double minPrice, Double maxPrice, Double rating, Double minDiscount,
            Boolean inStock, int page, int size) {

        log.info("Searching products for query: {}, brand: {}, category: {}, minPrice: {}, maxPrice: {}, rating: {}, discount: {}, inStock: {}",
                textQuery, brand, categoryPath, minPrice, maxPrice, rating, minDiscount, inStock);

        // NativeQuery search builders in Spring Data Elasticsearch 5.x
        // If query is blank/null, use MatchAll query
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    // Match query with boosting
                    if (textQuery != null && !textQuery.isBlank()) {
                        b.should(s -> s.match(m -> m.field("name").query(textQuery).boost(5.0f)));
                        b.should(s -> s.match(m -> m.field("brand").query(textQuery).boost(3.0f)));
                        b.should(s -> s.match(m -> m.field("description").query(textQuery).boost(1.0f)));
                        b.minimumShouldMatch("1");
                    } else {
                        b.must(m -> m.matchAll(ma -> ma));
                    }

                    // Filters
                    if (brand != null && !brand.isBlank()) {
                        b.filter(f -> f.term(t -> t.field("brand").value(brand)));
                    }
                    if (categoryPath != null && !categoryPath.isBlank()) {
                        b.filter(f -> f.term(t -> t.field("categoryPath").value(categoryPath)));
                    }
                    if (minPrice != null || maxPrice != null) {
                        b.filter(f -> f.range(r -> {
                            r.field("price");
                            if (minPrice != null) r.gte(JsonData.of(minPrice));
                            if (maxPrice != null) r.lte(JsonData.of(maxPrice));
                            return r;
                        }));
                    }
                    if (rating != null) {
                        b.filter(f -> f.range(r -> r.field("rating").gte(JsonData.of(rating))));
                    }
                    if (minDiscount != null) {
                        b.filter(f -> f.range(r -> r.field("discount").gte(JsonData.of(minDiscount))));
                    }
                    if (inStock != null) {
                        b.filter(f -> f.term(t -> t.field("inStock").value(inStock)));
                    }
                    return b;
                }))
                .withPageable(PageRequest.of(page, size))
                .build();

        try {
            SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
            List<ProductDocument> content = hits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            return new PageImpl<>(content, PageRequest.of(page, size), hits.getTotalHits());
        } catch (Exception e) {
            log.error("Elasticsearch query failed, falling back to database query simulation: {}", e.getMessage());
            // Fallback: search products from database if Elasticsearch is unavailable in local workspace dev/test
            Page<Product> dbProds;
            if (textQuery != null && !textQuery.isBlank()) {
                dbProds = productRepository.findAll((root, query1, cb) -> {
                    String likeQuery = "%" + textQuery.toLowerCase() + "%";
                    return cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.or(
                            cb.like(cb.lower(root.get("name")), likeQuery),
                            cb.like(cb.lower(root.get("brand")), likeQuery),
                            cb.like(cb.lower(root.get("description")), likeQuery)
                        )
                    );
                }, PageRequest.of(page, size));
            } else {
                dbProds = productRepository.findAll((root, query1, cb) -> cb.equal(root.get("isActive"), true), PageRequest.of(page, size));
            }
            List<ProductDocument> fallbackContent = dbProds.stream()
                    .map(product -> ProductDocument.builder()
                            .id(String.valueOf(product.getId()))
                            .name(product.getName())
                            .brand(product.getBrand())
                            .categoryPath(product.getCategory() != null ? product.getCategory().getName() : "")
                            .description(product.getDescription())
                            .price(product.getBasePrice().doubleValue())
                            .rating(product.getAverageRating().doubleValue())
                            .discount(product.getCalculatedDiscountPercent().doubleValue())
                            .inStock(product.getStockQty() > 0)
                            .slug(product.getSlug())
                            .mrp(product.getMrp() != null ? product.getMrp().doubleValue() : 0.0)
                            .reviewCount(product.getReviewCount())
                            .images(product.getImages() != null ? product.getImages().stream().map(com.swiftcart.entity.ProductImage::getImageUrl).collect(Collectors.toList()) : Collections.emptyList())
                            .build())
                    .collect(Collectors.toList());
            return new PageImpl<>(fallbackContent, PageRequest.of(page, size), dbProds.getTotalElements());
        }
    }

    @Cacheable(value = "searchSuggestions", key = "#prefix")
    public List<String> getAutocompleteSuggestions(String prefix) {
        log.info("Fetching search suggestions for: {}", prefix);
        if (prefix == null || prefix.isBlank()) {
            return Collections.emptyList();
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.match(m -> m.field("name").query(prefix)))
                .withPageable(PageRequest.of(0, 10))
                .build();

        try {
            SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
            return hits.stream()
                    .map(hit -> hit.getContent().getName())
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch search autocomplete from ES: {}", e.getMessage());
            // Fallback suggestion list from DB query
            return productRepository.findAll().stream()
                    .map(Product::getName)
                    .filter(name -> name.toLowerCase().contains(prefix.toLowerCase()))
                    .limit(10)
                    .collect(Collectors.toList());
        }
    }

    public List<String> getTrendingSearchKeywords() {
        // Mock trending keyword list for demonstration
        return List.of("iphone 15 pro", "wireless headphones", "mechanical keyboard", "running shoes", "smart watch");
    }
}
