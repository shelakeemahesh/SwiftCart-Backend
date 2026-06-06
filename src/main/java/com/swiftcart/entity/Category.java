package com.swiftcart.entity;
 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
 
@Entity
@Table(name = "categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String name;
 
    @Column(unique = true, nullable = false)
    private String slug;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"parent", "subCategories"})
    private List<Category> subCategories = new ArrayList<>();

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Constructors
    public Category() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }

    public List<Category> getSubCategories() { return subCategories; }
    public void setSubCategories(List<Category> subCategories) { this.subCategories = subCategories; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public boolean isActive() { return isActive != null && isActive; }
    public void setActive(Boolean active) { this.isActive = active; }

    // Builder
    public static CategoryBuilder builder() {
        return new CategoryBuilder();
    }

    public static class CategoryBuilder {
        private String name;
        private String slug;
        private Category parent;
        private List<Category> subCategories = new ArrayList<>();
        private String imageUrl;
        private Integer displayOrder = 0;
        private Boolean isActive = true;

        public CategoryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CategoryBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public CategoryBuilder parent(Category parent) {
            this.parent = parent;
            return this;
        }

        public CategoryBuilder subCategories(List<Category> subCategories) {
            this.subCategories = subCategories;
            return this;
        }

        public CategoryBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public CategoryBuilder displayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public CategoryBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Category build() {
            Category cat = new Category();
            cat.setName(this.name);
            cat.setSlug(this.slug);
            cat.setParent(this.parent);
            if (this.subCategories != null) {
                cat.setSubCategories(this.subCategories);
            }
            cat.setImageUrl(this.imageUrl);
            cat.setDisplayOrder(this.displayOrder);
            cat.setActive(this.isActive);
            return cat;
        }
    }
}
