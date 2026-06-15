package com.swiftcart.controller;

import com.swiftcart.dto.response.ApiResponse;

import com.swiftcart.entity.Category;
import com.swiftcart.entity.Product;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Transactional(readOnly = true)
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    public CategoryController(CategoryService categoryService, ProductRepository productRepository) {
        this.categoryService = categoryService;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getRootCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getRootCategories()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<Category>> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryBySlug(slug)));
    }

    @GetMapping("/{slug}/products")
    public ResponseEntity<ApiResponse<Page<Product>>> getCategoryProducts(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Category category = categoryService.getCategoryBySlug(slug);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Product> products = productRepository.findAll((root, query, cb) -> 
            cb.and(
                cb.or(
                    cb.equal(root.get("category").get("id"), category.getId()),
                    cb.equal(root.get("category").get("parent").get("id"), category.getId())
                ),
                cb.equal(root.get("isActive"), true)
            ), pageable);
            
        products.getContent().forEach(p -> {
            if (p.getImages() != null) p.getImages().size();
            if (p.getVariants() != null) p.getVariants().size();
        });
            
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
