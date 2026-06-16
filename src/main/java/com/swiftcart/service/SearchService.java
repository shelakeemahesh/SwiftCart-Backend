package com.swiftcart.service;

import com.swiftcart.entity.Product;
import com.swiftcart.entity.ProductDocument;
import com.swiftcart.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final ProductRepository productRepository;

    public SearchService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductDocument> searchProducts(
            String textQuery, String brand, String categoryPath,
            Double minPrice, Double maxPrice, Double rating, Double minDiscount,
            Boolean inStock, int page, int size) {

        log.info("Searching products for query: {}, brand: {}, category: {}, minPrice: {}, maxPrice: {}, rating: {}, discount: {}, inStock: {}",
                textQuery, brand, categoryPath, minPrice, maxPrice, rating, minDiscount, inStock);

        Specification<Product> spec = Specification.where((root, query, cb) -> cb.equal(root.get("isActive"), true));

        if (textQuery != null && !textQuery.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                String pattern = "%" + textQuery.toLowerCase().trim() + "%";
                return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("brand")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
                );
            });
        }

        if (brand != null && !brand.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("brand"), brand));
        }

        if (categoryPath != null && !categoryPath.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("name"), categoryPath));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("basePrice"), BigDecimal.valueOf(minPrice)));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("basePrice"), BigDecimal.valueOf(maxPrice)));
        }

        if (rating != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("averageRating"), BigDecimal.valueOf(rating)));
        }

        if (minDiscount != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("discountPercent"), BigDecimal.valueOf(minDiscount)));
        }

        if (inStock != null) {
            spec = spec.and((root, query, cb) -> {
                if (inStock) {
                    return cb.greaterThan(root.get("stockQty"), 0);
                } else {
                    return cb.equal(root.get("stockQty"), 0);
                }
            });
        }

        Page<Product> dbProds = productRepository.findAll(spec, PageRequest.of(page, size));

        List<ProductDocument> content = dbProds.stream()
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

        return new PageImpl<>(content, PageRequest.of(page, size), dbProds.getTotalElements());
    }

    @Cacheable(value = "searchSuggestions", key = "#prefix")
    public List<String> getAutocompleteSuggestions(String prefix) {
        log.info("Fetching search suggestions for: {}", prefix);
        if (prefix == null || prefix.isBlank()) {
            return Collections.emptyList();
        }
        return productRepository.findNamesByPrefix(prefix, PageRequest.of(0, 10));
    }

    public List<String> getTrendingSearchKeywords() {
        return List.of("iphone 15 pro", "wireless headphones", "mechanical keyboard", "running shoes", "smart watch");
    }
}
