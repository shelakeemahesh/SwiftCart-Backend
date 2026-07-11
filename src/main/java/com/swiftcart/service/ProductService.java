package com.swiftcart.service;

import com.swiftcart.entity.Category;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.ProductImage;
import com.swiftcart.entity.User;
import com.swiftcart.repository.CategoryRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.repository.ReviewRepository;
import com.swiftcart.kafka.producer.OrderEventProducer;
import com.swiftcart.util.SlugUtil;
import com.swiftcart.entity.FlashSale;
import com.swiftcart.repository.FlashSaleRepository;
import org.springframework.cache.annotation.Caching;
import java.time.LocalDateTime;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ReviewRepository reviewRepository;
    private final RedisFallbackService redisService;
    private final OrderEventProducer orderEventProducer;
    private final SearchService searchService;
    private final FlashSaleRepository flashSaleRepository;
    private final PriceHistoryService priceHistoryService;

    private ProductService self;

    @org.springframework.beans.factory.annotation.Autowired
    public void setSelf(@org.springframework.context.annotation.Lazy ProductService self) {
        this.self = self;
    }

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            S3Service s3Service,
            ReviewRepository reviewRepository,
            RedisFallbackService redisService,
            OrderEventProducer orderEventProducer,
            SearchService searchService,
            FlashSaleRepository flashSaleRepository,
            PriceHistoryService priceHistoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
        this.reviewRepository = reviewRepository;
        this.redisService = redisService;
        this.orderEventProducer = orderEventProducer;
        this.searchService = searchService;
        this.flashSaleRepository = flashSaleRepository;
        this.priceHistoryService = priceHistoryService;
    }

    public Page<Product> listProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Cacheable(value = "productLists", key = "T(java.util.Objects).hash(#categoryId, #brand, #minPrice, #maxPrice, #rating, #discount, #inStock, #page, #size, #sort, #sellerId)")
    public Page<Product> listProducts(
            Long categoryId,
            String brand,
            Double minPrice,
            Double maxPrice,
            Double rating,
            Double discount,
            boolean inStock,
            int page,
            int size,
            String sort,
            Long sellerId) {
        
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        if (sortField.equals("price")) {
            sortField = "basePrice";
        } else if (sortField.equals("rating")) {
            sortField = "averageRating";
        }
        
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(sortField);
        if (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")) {
            sortObj = sortObj.descending();
        } else {
            sortObj = sortObj.ascending();
        }
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);

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

        result.getContent();
        return result;
    }

    @Cacheable(value = "productLists", key = "T(java.util.Objects).hash(#slug, #page, #size)")
    public Page<Product> getProductsByCategorySlug(String slug, int page, int size) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Category not found with slug: " + slug));
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        
        Page<Product> products = productRepository.findAll((root, query, cb) -> 
            cb.and(
                cb.or(
                    cb.equal(root.get("category").get("id"), category.getId()),
                    cb.equal(root.get("category").get("parent").get("id"), category.getId())
                ),
                cb.equal(root.get("isActive"), true)
            ), pageable);
            
        products.getContent();
        return products;
    }

    @Cacheable(value = "trendingProducts")
    public List<Product> getTrendingProducts() {
        return productRepository.findTop20ByIsActiveTrueOrderBySoldCountDesc();
    }

    @Cacheable(value = "newArrivals")
    public List<Product> getNewArrivals() {
        return productRepository.findTop20ByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Cacheable(value = "flashDeals")
    public List<Product> getFlashDeals() {
        List<FlashSale> activeSales = flashSaleRepository.findActiveFlashSales(LocalDateTime.now());
        return activeSales.stream()
                .map(FlashSale::getProduct)
                .filter(Product::isActive)
                .collect(java.util.stream.Collectors.toList());
    }

    @Cacheable(value = "productDetails", key = "#slug")
    public Product getProductBySlug(String slug) {
        log.info("Fetching product details from database for slug: {}", slug);
        return productRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new RuntimeException("Product not found with slug: " + slug));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "productDetails", key = "#product.slug"),
        @CacheEvict(value = "productLists", allEntries = true),
        @CacheEvict(value = "trendingProducts", allEntries = true),
        @CacheEvict(value = "newArrivals", allEntries = true),
        @CacheEvict(value = "flashDeals", allEntries = true)
    })
    public Product saveProduct(Product product) {
        if (product.getSlug() == null || product.getSlug().isBlank()) {
            product.setSlug(generateUniqueSlug(product.getName()));
        }

        Product saved = productRepository.save(product);
        try {
            priceHistoryService.recordPriceChange(saved, saved.getBasePrice());
        } catch (Exception e) {
            log.error("Failed to record price change on saveProduct", e);
        }
        publishIndexingEvent(saved);
        return saved;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "productDetails", key = "#slug"),
        @CacheEvict(value = "productLists", allEntries = true),
        @CacheEvict(value = "trendingProducts", allEntries = true),
        @CacheEvict(value = "newArrivals", allEntries = true),
        @CacheEvict(value = "flashDeals", allEntries = true)
    })
    public Product updateProduct(String slug, Product updatedProduct) {
        Product existing = productRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new RuntimeException("Product not found with slug: " + slug));

        existing.setName(updatedProduct.getName());
        existing.setBrand(updatedProduct.getBrand());
        existing.setDescription(updatedProduct.getDescription());
        existing.setBasePrice(updatedProduct.getBasePrice());
        existing.setMrp(updatedProduct.getMrp());
        existing.setStockQty(updatedProduct.getStockQty());
        existing.setActive(updatedProduct.isActive());
        existing.setFeatured(updatedProduct.isFeatured());
        existing.setHighlights(updatedProduct.getHighlights());
        existing.setSpecifications(updatedProduct.getSpecifications());

        Product saved = productRepository.save(existing);
        try {
            priceHistoryService.recordPriceChange(saved, saved.getBasePrice());
        } catch (Exception e) {
            log.error("Failed to record price change on updateProduct", e);
        }
        publishIndexingEvent(saved);
        return saved;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "productDetails", allEntries = true),
        @CacheEvict(value = "productLists", allEntries = true),
        @CacheEvict(value = "trendingProducts", allEntries = true),
        @CacheEvict(value = "newArrivals", allEntries = true),
        @CacheEvict(value = "flashDeals", allEntries = true)
    })
    public void deleteProduct(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setActive(false);
        Product saved = productRepository.save(p);
        publishIndexingEvent(saved);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "productDetails", allEntries = true),
        @CacheEvict(value = "productLists", allEntries = true),
        @CacheEvict(value = "trendingProducts", allEntries = true),
        @CacheEvict(value = "newArrivals", allEntries = true),
        @CacheEvict(value = "flashDeals", allEntries = true)
    })
    public void updateStock(Long id, int qty) {
        Product p = productRepository.findAndLockById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setStockQty(qty);
        Product saved = productRepository.save(p);
        publishIndexingEvent(saved);
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtil.toSlug(name);
        String currentSlug = baseSlug;
        int counter = 1;

        while (productRepository.existsBySlug(currentSlug)) {
            currentSlug = baseSlug + "-" + counter;
            counter++;
        }
        return currentSlug;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "productDetails", allEntries = true),
        @CacheEvict(value = "productLists", allEntries = true),
        @CacheEvict(value = "trendingProducts", allEntries = true),
        @CacheEvict(value = "newArrivals", allEntries = true),
        @CacheEvict(value = "flashDeals", allEntries = true)
    })
    public void recalculateAverageRating(Long productId, BigDecimal newAverage, int newCount) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setAverageRating(newAverage);
        product.setReviewCount(newCount);
        Product saved = productRepository.save(product);
        publishIndexingEvent(saved);
        log.info("Recalculated rating for product ID {}: avg = {}, count = {}", productId, newAverage, newCount);
    }

    @Async
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "productDetails", allEntries = true),
        @CacheEvict(value = "productLists", allEntries = true),
        @CacheEvict(value = "trendingProducts", allEntries = true),
        @CacheEvict(value = "newArrivals", allEntries = true),
        @CacheEvict(value = "flashDeals", allEntries = true)
    })
    public void recalculateProductRatingAsync(Long productId) {
        log.info("Recalculating average rating asynchronously for product ID: {}", productId);
        try {
            List<com.swiftcart.entity.Review> reviews = reviewRepository.findByProductId(productId);
            if (reviews.isEmpty()) {
                self.recalculateAverageRating(productId, BigDecimal.ZERO, 0);
                return;
            }

            double sum = 0.0;
            for (com.swiftcart.entity.Review r : reviews) {
                sum += r.getRating();
            }
            double average = sum / reviews.size();
            BigDecimal averageRating = BigDecimal.valueOf(average).setScale(2, java.math.RoundingMode.HALF_UP);
            int count = reviews.size();

            self.recalculateAverageRating(productId, averageRating, count);
        } catch (Exception e) {
            log.error("Failed to recalculate rating for product ID {}: {}", productId, e.getMessage());
        }
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "productDetails", allEntries = true),
        @CacheEvict(value = "productLists", allEntries = true),
        @CacheEvict(value = "trendingProducts", allEntries = true),
        @CacheEvict(value = "newArrivals", allEntries = true),
        @CacheEvict(value = "flashDeals", allEntries = true)
    })
    public List<String> uploadProductImages(Long productId, byte[] fileBytes, String originalFilename, String contentType) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        try {
            ByteArrayOutputStream mainOs = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(fileBytes))
                    .size(800, 1200)
                    .outputFormat("jpg")
                    .toOutputStream(mainOs);
            byte[] mainBytes = mainOs.toByteArray();

            ByteArrayOutputStream thumbOs = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(fileBytes))
                    .size(400, 600)
                    .outputFormat("jpg")
                    .toOutputStream(thumbOs);
            byte[] thumbBytes = thumbOs.toByteArray();

            String mainUrl = s3Service.uploadFile(mainBytes, "main_" + originalFilename, "image/jpeg");
            String thumbUrl = s3Service.uploadFile(thumbBytes, "thumb_" + originalFilename, "image/jpeg");

            ProductImage mainImg = ProductImage.builder()
                    .product(product)
                    .imageUrl(mainUrl)
                    .isPrimary(true)
                    .displayOrder(0)
                    .build();

            ProductImage thumbImg = ProductImage.builder()
                    .product(product)
                    .imageUrl(thumbUrl)
                    .isPrimary(false)
                    .displayOrder(1)
                    .build();

            if (product.getImages() == null) {
                product.setImages(new ArrayList<>());
            }
            product.getImages().add(mainImg);
            product.getImages().add(thumbImg);

            Product saved = productRepository.save(product);
            publishIndexingEvent(saved);

            return List.of(mainUrl, thumbUrl);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process and upload product images", e);
        }
    }

    @Async
    public void processCsvImport(String jobId, byte[] csvBytes, Long sellerId) {
        String statusKey = "csv-import:status:" + jobId;
        redisService.set(statusKey, "PROCESSING", Duration.ofHours(2));

        User seller = userRepository.findById(sellerId).orElse(null);
        if (seller == null) {
            redisService.set(statusKey, "FAILED: Seller not found", Duration.ofHours(2));
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvBytes), StandardCharsets.UTF_8))) {
            String line;
            int imported = 0;
            int failed = 0;
            
            String header = reader.readLine();

            while ((line = reader.readLine()) != null) {
                try {
                    String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (tokens.length < 5) continue;

                    String name = tokens[0].replace("\"", "").trim();
                    String brand = tokens[1].replace("\"", "").trim();
                    String categoryName = tokens[2].replace("\"", "").trim();
                    BigDecimal basePrice = new BigDecimal(tokens[3].trim());
                    BigDecimal mrp = new BigDecimal(tokens[4].trim());
                    int stock = java.util.concurrent.ThreadLocalRandom.current().nextInt(20, 26);
                    String desc = tokens.length > 6 ? tokens[6].replace("\"", "").trim() : "";
                    String imageUrlList = tokens.length > 7 ? tokens[7].replace("\"", "").trim() : "";

                    String catSlug = SlugUtil.toSlug(categoryName);
                    Category cat = categoryRepository.findBySlug(catSlug)
                            .orElseGet(() -> categoryRepository.save(Category.builder()
                                    .name(categoryName)
                                    .slug(catSlug)
                                    .isActive(true)
                                    .build()));

                    Product p = Product.builder()
                            .name(name)
                            .brand(brand)
                            .category(cat)
                            .basePrice(basePrice)
                            .mrp(mrp)
                            .stockQty(stock)
                            .description(desc)
                            .seller(seller)
                            .isActive(true)
                            .build();

                    if (!imageUrlList.isEmpty()) {
                        List<ProductImage> pImages = new ArrayList<>();
                        String[] urls = imageUrlList.split("\\|");
                        for (int i = 0; i < urls.length; i++) {
                            String url = urls[i].trim();
                            if (!url.isEmpty()) {
                                pImages.add(ProductImage.builder()
                                        .product(p)
                                        .imageUrl(url)
                                        .isPrimary(i == 0)
                                        .displayOrder(i)
                                        .build());
                            }
                        }
                        p.setImages(pImages);
                    }

                    saveProduct(p);
                    imported++;
                } catch (Exception e) {
                    failed++;
                    log.error("Failed to import CSV row: {} due to {}", line, e.getMessage());
                }
            }

            redisService.set(statusKey, "SUCCESS: Imported " + imported + " products, Failed: " + failed, Duration.ofHours(2));
        } catch (Exception e) {
            log.error("Error reading CSV byte stream", e);
            redisService.set(statusKey, "FAILED: " + e.getMessage(), Duration.ofHours(2));
        }
    }

    public String getCsvImportStatus(String jobId) {
        String status = redisService.get("csv-import:status:" + jobId);
        return status != null ? status : "NOT_FOUND";
    }

    private void publishIndexingEvent(Product product) {
        if (orderEventProducer.isKafkaEnabled()) {
            orderEventProducer.publishProductIndexing(product.getId());
        } else {
            searchService.indexProduct(product.getId());
        }
    }
}
