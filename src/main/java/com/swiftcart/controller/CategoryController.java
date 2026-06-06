package com.swiftcart.controller;

import com.swiftcart.entity.Category;
import com.swiftcart.entity.Product;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    public CategoryController(CategoryService categoryService, ProductRepository productRepository) {
        this.categoryService = categoryService;
        this.productRepository = productRepository;
    }

    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<Category>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Category> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @GetMapping("/{slug}/products")
    public ResponseEntity<Page<Product>> getCategoryProducts(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Category category = categoryService.getCategoryBySlug(slug);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Product> products = productRepository.findAll((root, query, cb) -> 
            cb.and(cb.equal(root.get("category").get("id"), category.getId()), cb.equal(root.get("isActive"), true)), pageable);
            
        return ResponseEntity.ok(products);
    }
}
