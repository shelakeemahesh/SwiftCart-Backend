package com.swiftcart.controller;

import com.swiftcart.entity.ProductDocument;
import com.swiftcart.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductDocument>> fullTextSearch(
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

    @GetMapping("/suggest")
    public ResponseEntity<List<String>> getAutocompleteSuggestions(@RequestParam("q") String prefix) {
        return ResponseEntity.ok(searchService.getAutocompleteSuggestions(prefix));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrendingSearchKeywords() {
        return ResponseEntity.ok(searchService.getTrendingSearchKeywords());
    }
}
