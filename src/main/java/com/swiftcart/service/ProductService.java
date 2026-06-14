package com.swiftcart.service;

import com.swiftcart.config.KafkaConfig;
import com.swiftcart.entity.Category;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.ProductImage;
import com.swiftcart.entity.User;
import com.swiftcart.repository.CategoryRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.util.SlugUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
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
import java.util.Map;
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisFallbackService redisService;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            S3Service s3Service,
            java.util.Optional<KafkaTemplate<String, Object>> kafkaTemplate,
            RedisFallbackService redisService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
        this.kafkaTemplate = kafkaTemplate.orElse(null);
        this.redisService = redisService;
    }

    public Page<Product> listProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Cacheable(value = "productDetails", key = "#slug")
    public Product getProductBySlug(String slug) {
        log.info("Fetching product details from database for slug: {}", slug);
        return productRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new RuntimeException("Product not found with slug: " + slug));
    }

    @Transactional
    @CacheEvict(value = "productDetails", key = "#product.slug")
    public Product saveProduct(Product product) {
        // Handle slug auto-generation
        if (product.getSlug() == null || product.getSlug().isBlank()) {
            product.setSlug(generateUniqueSlug(product.getName()));
        }

        Product saved = productRepository.save(product);

        // Publish to Kafka for Elasticsearch Indexing
        publishIndexingEvent(saved);

        return saved;
    }

    @Transactional
    @CacheEvict(value = "productDetails", key = "#slug")
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
        publishIndexingEvent(saved);
        return saved;
    }

    @Transactional
    @CacheEvict(value = "productDetails", allEntries = true)
    public void deleteProduct(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setActive(false);
        productRepository.save(p);
    }

    @Transactional
    @CacheEvict(value = "productDetails", allEntries = true)
    public void updateStock(Long id, int qty) {
        Product p = productRepository.findAndLockById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setStockQty(qty);
        productRepository.save(p);
    }

    // Slug auto-generation handling duplicates
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

    // Recalculates average rating asynchronously (e.g. from Kafka consumer or service call)
    @Transactional
    @CacheEvict(value = "productDetails", allEntries = true)
    public void recalculateAverageRating(Long productId, BigDecimal newAverage, int newCount) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setAverageRating(newAverage);
        product.setReviewCount(newCount);
        productRepository.save(product);
        publishIndexingEvent(product);
        log.info("Recalculated rating for product ID {}: avg = {}, count = {}", productId, newAverage, newCount);
    }

    // Image resizing to 800x1200 and 400x600 using Thumbnailator, and S3 upload
    public List<String> uploadProductImages(Long productId, byte[] fileBytes, String originalFilename, String contentType) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        try {
            // Resize to 800x1200 (Main Display)
            ByteArrayOutputStream mainOs = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(fileBytes))
                    .size(800, 1200)
                    .outputFormat("jpg")
                    .toOutputStream(mainOs);
            byte[] mainBytes = mainOs.toByteArray();

            // Resize to 400x600 (Thumbnail)
            ByteArrayOutputStream thumbOs = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(fileBytes))
                    .size(400, 600)
                    .outputFormat("jpg")
                    .toOutputStream(thumbOs);
            byte[] thumbBytes = thumbOs.toByteArray();

            // Upload both
            String mainUrl = s3Service.uploadFile(mainBytes, "main_" + originalFilename, "image/jpeg");
            String thumbUrl = s3Service.uploadFile(thumbBytes, "thumb_" + originalFilename, "image/jpeg");

            // Persist as product images in DB
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

            productRepository.save(product);
            publishIndexingEvent(product);

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
            // Skip header
            String header = reader.readLine();

            while ((line = reader.readLine()) != null) {
                try {
                    // Simple CSV parser split on comma (handling quoted values if required, simple split for stub)
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

                    // Find or create category
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
        try {
            // Send to Kafka for Elasticsearch indexing
            kafkaTemplate.send(KafkaConfig.PRODUCT_INDEXING_TOPIC, String.valueOf(product.getId()), product.getId());
            log.info("Published product indexing event to Kafka for ID: {}", product.getId());
        } catch (Exception e) {
            log.error("Failed to publish indexing event for product {}: {}", product.getId(), e.getMessage());
        }
    }
}
