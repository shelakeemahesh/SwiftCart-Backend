package com.swiftcart.service;

import com.swiftcart.entity.Category;
import com.swiftcart.repository.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(value = "categoryTree")
    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategoriesWithSubcategories();
    }

    private void initializeSubCategories(Category category) {
        if (category.getSubCategories() != null) {
            org.hibernate.Hibernate.initialize(category.getSubCategories());
            category.getSubCategories().forEach(this::initializeSubCategories);
        }
    }

    @Transactional(readOnly = true)
    public Category getCategoryBySlug(String slug) {
        java.util.Optional<Category> catOpt = categoryRepository.findBySlug(slug);
        if (catOpt.isEmpty() && slug != null && slug.matches("\\d+")) {
            try {
                catOpt = categoryRepository.findById(Long.parseLong(slug));
            } catch (Exception ignored) {}
        }
        return catOpt.orElseThrow(() -> new RuntimeException("Category not found with identifier: " + slug));
    }

    @Transactional
    @CacheEvict(value = "categoryTree", allEntries = true)
    public Category createCategory(Category category) {
        if (categoryRepository.findBySlug(category.getSlug()).isPresent()) {
            throw new RuntimeException("Category slug already exists: " + category.getSlug());
        }
        return categoryRepository.save(category);
    }
}
