package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@Transactional(readOnly = true)
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
    public ResponseEntity<ApiResponse<Page<Product>>> listProducts(
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

        Page<Product> result = productService.listProducts(
                categoryId, brand, minPrice, maxPrice, rating, discount, inStock, page, size, sort, sellerId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<Product>> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductBySlug(slug)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductDocument>>> searchProducts(
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
        return ResponseEntity.ok(ApiResponse.success(searchService.searchProducts(
                query, brand, categoryPath, minPrice, maxPrice, rating, discount, inStock, page, size)));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<Product>>> getTrendingProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getTrendingProducts()));
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<ApiResponse<List<Product>>> getNewArrivals() {
        return ResponseEntity.ok(ApiResponse.success(productService.getNewArrivals()));
    }

    @GetMapping("/deals")
    public ResponseEntity<ApiResponse<List<Product>>> getFlashDeals() {
        return ResponseEntity.ok(ApiResponse.success(productService.getFlashDeals()));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<Product>>> getRelatedProducts(@PathVariable Long id, @RequestParam(defaultValue = "5") int limit) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<Product> products = productRepository.findRelatedProducts(
                product.getCategory().getId(), id, PageRequest.of(0, limit));
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}/frequently-bought")
    public ResponseEntity<ApiResponse<List<Product>>> getFrequentlyBoughtTogether(@PathVariable Long id) {
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<Product> list = productRepository.findRelatedProducts(
                product.getCategory().getId(), id, PageRequest.of(0, 3));
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
