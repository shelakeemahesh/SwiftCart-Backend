package com.swiftcart.entity;

import java.util.ArrayList;
import java.util.List;

public class ProductDocument {

    private String id;
    private String name;
    private String brand;
    private String categoryPath;
    private String description;
    private Double price;
    private Double rating;
    private Double discount;
    private Boolean inStock;
    private String slug;
    private Double mrp;
    private Integer reviewCount;
    private List<String> images = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    public ProductDocument() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategoryPath() { return categoryPath; }
    public void setCategoryPath(String categoryPath) { this.categoryPath = categoryPath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Double getMrp() { return mrp; }
    public void setMrp(Double mrp) { this.mrp = mrp; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public static ProductDocumentBuilder builder() {
        return new ProductDocumentBuilder();
    }

    public static class ProductDocumentBuilder {
        private String id;
        private String name;
        private String brand;
        private String categoryPath;
        private String description;
        private Double price;
        private Double rating;
        private Double discount;
        private Boolean inStock;
        private String slug;
        private Double mrp;
        private Integer reviewCount;
        private List<String> images = new ArrayList<>();
        private List<String> tags = new ArrayList<>();

        public ProductDocumentBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ProductDocumentBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProductDocumentBuilder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public ProductDocumentBuilder categoryPath(String categoryPath) {
            this.categoryPath = categoryPath;
            return this;
        }

        public ProductDocumentBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ProductDocumentBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public ProductDocumentBuilder rating(Double rating) {
            this.rating = rating;
            return this;
        }

        public ProductDocumentBuilder discount(Double discount) {
            this.discount = discount;
            return this;
        }

        public ProductDocumentBuilder inStock(Boolean inStock) {
            this.inStock = inStock;
            return this;
        }

        public ProductDocumentBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public ProductDocumentBuilder mrp(Double mrp) {
            this.mrp = mrp;
            return this;
        }

        public ProductDocumentBuilder reviewCount(Integer reviewCount) {
            this.reviewCount = reviewCount;
            return this;
        }

        public ProductDocumentBuilder images(List<String> images) {
            this.images = images;
            return this;
        }

        public ProductDocumentBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public ProductDocument build() {
            ProductDocument doc = new ProductDocument();
            doc.setId(this.id);
            doc.setName(this.name);
            doc.setBrand(this.brand);
            doc.setCategoryPath(this.categoryPath);
            doc.setDescription(this.description);
            doc.setPrice(this.price);
            doc.setRating(this.rating);
            doc.setDiscount(this.discount);
            doc.setInStock(this.inStock);
            doc.setSlug(this.slug);
            doc.setMrp(this.mrp);
            doc.setReviewCount(this.reviewCount);
            if (this.images != null) doc.setImages(this.images);
            if (this.tags != null) doc.setTags(this.tags);
            return doc;
        }
    }
}
