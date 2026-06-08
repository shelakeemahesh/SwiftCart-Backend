package com.swiftcart.controller;

import com.swiftcart.entity.FlashSale;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.ProductDocument;
import com.swiftcart.repository.FlashSaleRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.service.ProductService;
import com.swiftcart.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final SearchService searchService;
    private final FlashSaleRepository flashSaleRepository;

    public ProductController(
            ProductService productService,
            ProductRepository productRepository,
            SearchService searchService,
            FlashSaleRepository flashSaleRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.searchService = searchService;
        this.flashSaleRepository = flashSaleRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Product>> listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) Double discount,
            @RequestParam(defaultValue = "false") boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            @RequestParam(required = false) Long sellerId) {

        // Setup Pageable with sorts
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        // Map frontend base price sorts
        if (sortField.equals("price")) {
            sortField = "basePrice";
        } else if (sortField.equals("rating")) {
            sortField = "averageRating";
        }
        Sort sortObj = Sort.by(sortField);
        if (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")) {
            sortObj = sortObj.descending();
        } else {
            sortObj = sortObj.ascending();
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<Product> result = productRepository.findAll((root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.equal(root.get("isActive"), true));

            if (categoryId != null) {
                predicates.add(cb.or(
                    cb.equal(root.get("category").get("id"), categoryId),
                    cb.equal(root.get("category").get("parent").get("id"), categoryId)
                ));
            }
            if (sellerId != null) {
                predicates.add(cb.equal(root.get("seller").get("id"), sellerId));
            }
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(root.get("brand"), brand));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), java.math.BigDecimal.valueOf(minPrice)));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), java.math.BigDecimal.valueOf(maxPrice)));
            }
            if (rating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), java.math.BigDecimal.valueOf(rating)));
            }
            if (discount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("discountPercent"), java.math.BigDecimal.valueOf(discount)));
            }
            if (inStock) {
                predicates.add(cb.greaterThan(root.get("stockQty"), 0));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Product> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDocument>> searchProducts(
            @RequestParam("q") String query,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String categoryPath,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) Double discount,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.searchProducts(
                query, brand, categoryPath, minPrice, maxPrice, rating, discount, inStock, page, size));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<Product>> getTrendingProducts() {
        return ResponseEntity.ok(productRepository.findTop20ByIsActiveTrueOrderBySoldCountDesc());
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<List<Product>> getNewArrivals() {
        return ResponseEntity.ok(productRepository.findTop20ByIsActiveTrueOrderByCreatedAtDesc());
    }

    @GetMapping("/deals")
    public ResponseEntity<List<Product>> getFlashDeals() {
        List<FlashSale> activeSales = flashSaleRepository.findActiveFlashSales(LocalDateTime.now());
        List<Product> products = activeSales.stream()
                .map(FlashSale::getProduct)
                .filter(Product::isActive)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<Product>> getRelatedProducts(@PathVariable Long id, @RequestParam(defaultValue = "5") int limit) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ResponseEntity.ok(productRepository.findRelatedProducts(
                product.getCategory().getId(), id, PageRequest.of(0, limit)));
    }

    @GetMapping("/{id}/frequently-bought")
    public ResponseEntity<List<Product>> getFrequentlyBoughtTogether(@PathVariable Long id) {
        // Simulating frequently bought together products
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<Product> list = productRepository.findRelatedProducts(
                product.getCategory().getId(), id, PageRequest.of(0, 3));
        return ResponseEntity.ok(list);
    }
}
