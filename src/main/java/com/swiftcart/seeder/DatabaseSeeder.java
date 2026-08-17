package com.swiftcart.seeder;

import com.swiftcart.entity.Category;
import com.swiftcart.entity.FlashSale;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.ProductImage;
import com.swiftcart.entity.User;
import com.swiftcart.enums.Role;
import com.swiftcart.repository.*;
import com.swiftcart.service.ai.ProductVectorSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile({"dev", "prod"})
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductVectorSyncService productVectorSyncService;
    private final CartRepository cartRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ReviewRepository reviewRepository;
    private final ProductPriceHistoryRepository productPriceHistoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final RazorpayPaymentRepository razorpayPaymentRepository;
    private final PriceDropAlertRepository priceDropAlertRepository;
    private final FlashSaleRepository flashSaleRepository;

    private static final String[] ROOT_CATEGORIES = {
        "Electronics", "Fashion", "Home", "Grocery", "Beauty", "Sports", "Toys", "Books"
    };

    public DatabaseSeeder(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ProductVariantRepository productVariantRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ProductVectorSyncService productVectorSyncService,
            CartRepository cartRepository,
            WishlistItemRepository wishlistItemRepository,
            ReviewRepository reviewRepository,
            ProductPriceHistoryRepository productPriceHistoryRepository,
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            RazorpayPaymentRepository razorpayPaymentRepository,
            PriceDropAlertRepository priceDropAlertRepository,
            FlashSaleRepository flashSaleRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productVectorSyncService = productVectorSyncService;
        this.cartRepository = cartRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.reviewRepository = reviewRepository;
        this.productPriceHistoryRepository = productPriceHistoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.razorpayPaymentRepository = razorpayPaymentRepository;
        this.priceDropAlertRepository = priceDropAlertRepository;
        this.flashSaleRepository = flashSaleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Always Ensure Seed Users (Admin & Seller) Exist with Verified Status & Updated Passwords
        User defaultSeller = userRepository.findByEmail("seller_seed@swiftcart.com")
                .or(() -> userRepository.findByPhone("9999999999"))
                .map(s -> {
                    s.setPasswordHash(passwordEncoder.encode("seed123"));
                    s.setRole(Role.SELLER);
                    s.setVerified(true);
                    return userRepository.save(s);
                })
                .orElseGet(() -> {
                    User seller = User.builder()
                            .name("Seed Seller")
                            .email("seller_seed@swiftcart.com")
                            .passwordHash(passwordEncoder.encode("seed123"))
                            .role(Role.SELLER)
                            .phone("9999999999")
                            .isVerified(true)
                            .build();
                    return userRepository.save(seller);
                });

        userRepository.findByEmail("admin@swiftcart.com")
                .or(() -> userRepository.findByPhone("8888888888"))
                .map(a -> {
                    a.setPasswordHash(passwordEncoder.encode("admin123"));
                    a.setRole(Role.ADMIN);
                    a.setVerified(true);
                    return userRepository.save(a);
                })
                .orElseGet(() -> {
                    User admin = User.builder()
                            .name("SwiftCart Admin")
                            .email("admin@swiftcart.com")
                            .passwordHash(passwordEncoder.encode("admin123"))
                            .role(Role.ADMIN)
                            .phone("8888888888")
                            .isVerified(true)
                            .build();
                    return userRepository.save(admin);
                });

        userRepository.findByEmail("mahesh@swiftcart.com")
                .or(() -> userRepository.findByPhone("9503072201"))
                .map(m -> {
                    m.setPasswordHash(passwordEncoder.encode("admin123"));
                    m.setRole(Role.ADMIN);
                    m.setVerified(true);
                    return userRepository.save(m);
                })
                .orElseGet(() -> {
                    User admin = User.builder()
                            .name("Mahesh Admin")
                            .email("mahesh@swiftcart.com")
                            .passwordHash(passwordEncoder.encode("admin123"))
                            .role(Role.ADMIN)
                            .phone("9503072201")
                            .isVerified(true)
                            .build();
                    return userRepository.save(admin);
                });

        long currentCount = productRepository.count();
        boolean needsReSeed = (currentCount != 120 || flashSaleRepository.count() == 0);

        if (!needsReSeed) {
            log.info("Database already seeded with 120 curated products and flash sales. Skipping product catalog seeder...");
            return;
        }

        log.info("Starting curated catalog database seeding (15 products per category across 8 categories = 120 items)...");

        // 2. Ensure the 8 Root Categories exist
        Map<String, Category> rootCategories = new HashMap<>();
        for (int i = 0; i < ROOT_CATEGORIES.length; i++) {
            String rootName = ROOT_CATEGORIES[i];
            final int displayOrder = i;
            Category root = categoryRepository.findByName(rootName).orElseGet(() -> {
                Category cat = new Category();
                cat.setName(rootName);
                cat.setSlug(toSlug(rootName));
                cat.setActive(true);
                cat.setDisplayOrder(displayOrder);
                return categoryRepository.save(cat);
            });
            // Ensure slug is clean
            if (root.getSlug() == null || !root.getSlug().equalsIgnoreCase(toSlug(rootName))) {
                root.setSlug(toSlug(rootName));
                root.setDisplayOrder(displayOrder);
                root.setActive(true);
                root = categoryRepository.save(root);
            }
            rootCategories.put(rootName, root);
        }

        // 3. Clear old products if replacing legacy seed data in foreign key order
        if (currentCount > 0) {
            log.info("Cleaning up {} old products to replace with 120 curated items...", currentCount);
            try {
                flashSaleRepository.deleteAllInBatch();
                priceDropAlertRepository.deleteAllInBatch();
                orderItemRepository.deleteAllInBatch();
                razorpayPaymentRepository.deleteAllInBatch();
                orderRepository.deleteAllInBatch();
                cartRepository.deleteAllInBatch();
                wishlistItemRepository.deleteAllInBatch();
                productPriceHistoryRepository.deleteAllInBatch();
                reviewRepository.deleteAllInBatch();
                productVariantRepository.deleteAllInBatch();
                productImageRepository.deleteAllInBatch();
                productRepository.deleteAllInBatch();
            } catch (Exception ex) {
                log.warn("Notice during cleanup: {}", ex.getMessage());
            }
        }

        // 4. Seed all 120 Curated Products
        List<CuratedSeedData.SeedItem> seedItems = CuratedSeedData.getCuratedProducts();
        List<Product> savedProducts = new ArrayList<>();

        for (int idx = 0; idx < seedItems.size(); idx++) {
            CuratedSeedData.SeedItem item = seedItems.get(idx);
            Category category = rootCategories.get(item.category);
            if (category == null) {
                category = rootCategories.get("Home");
            }

            Product product = new Product();
            product.setName(item.name);
            product.setBrand(item.brand);
            product.setCategory(category);
            product.setBasePrice(item.price);
            product.setMrp(item.mrp);
            product.setStockQty(item.stockQty);
            product.setReviewCount(item.reviewCount);
            product.setHighlights(item.highlights);
            product.setSpecifications(item.specifications);
            product.setDescription(item.description);
            product.setSeller(defaultSeller);
            product.setActive(true);
            product.setFeatured(idx % 3 == 0);

            // Distinct metric assignments for clear separation between Trending, Best Sellers, and New Arrivals
            int calculatedSoldCount = 150;
            BigDecimal assignedRating = item.rating;
            LocalDateTime assignedCreatedAt = LocalDateTime.now().minusDays(10 + (idx % 20));

            // Trending Now (highest soldCount)
            if (idx == 0) { calculatedSoldCount = 4950; }
            else if (idx == 16) { calculatedSoldCount = 4850; }
            else if (idx == 105) { calculatedSoldCount = 4800; }
            else if (idx == 60) { calculatedSoldCount = 4700; }
            else if (idx == 83) { calculatedSoldCount = 4600; }
            else if (idx == 31) { calculatedSoldCount = 4500; }
            else if (idx == 47) { calculatedSoldCount = 4400; }
            else if (idx == 91) { calculatedSoldCount = 4300; }
            else if (idx == 2) { calculatedSoldCount = 4200; }
            else if (idx == 18) { calculatedSoldCount = 4100; }
            else if (idx == 63) { calculatedSoldCount = 4000; }
            else if (idx == 33) { calculatedSoldCount = 3900; }
            // Best Sellers (highest rating score 4.90 - 4.95)
            else if (idx == 1) { assignedRating = BigDecimal.valueOf(4.95); calculatedSoldCount = 2200; }
            else if (idx == 90) { assignedRating = BigDecimal.valueOf(4.95); calculatedSoldCount = 2150; }
            else if (idx == 106) { assignedRating = BigDecimal.valueOf(4.95); calculatedSoldCount = 2100; }
            else if (idx == 15) { assignedRating = BigDecimal.valueOf(4.94); calculatedSoldCount = 2050; }
            else if (idx == 30) { assignedRating = BigDecimal.valueOf(4.93); calculatedSoldCount = 1950; }
            else if (idx == 46) { assignedRating = BigDecimal.valueOf(4.93); calculatedSoldCount = 1900; }
            else if (idx == 67) { assignedRating = BigDecimal.valueOf(4.92); calculatedSoldCount = 1850; }
            else if (idx == 80) { assignedRating = BigDecimal.valueOf(4.92); calculatedSoldCount = 1800; }
            else if (idx == 6) { assignedRating = BigDecimal.valueOf(4.91); calculatedSoldCount = 2300; }
            else if (idx == 20) { assignedRating = BigDecimal.valueOf(4.90); calculatedSoldCount = 1750; }
            else if (idx == 34) { assignedRating = BigDecimal.valueOf(4.90); calculatedSoldCount = 1700; }
            else if (idx == 68) { assignedRating = BigDecimal.valueOf(4.90); calculatedSoldCount = 1650; }
            // New Arrivals (created within the last 2-24 hours)
            else if (idx == 4) { assignedCreatedAt = LocalDateTime.now().minusHours(2); calculatedSoldCount = 450; }
            else if (idx == 17) { assignedCreatedAt = LocalDateTime.now().minusHours(4); calculatedSoldCount = 380; }
            else if (idx == 32) { assignedCreatedAt = LocalDateTime.now().minusHours(6); calculatedSoldCount = 520; }
            else if (idx == 45) { assignedCreatedAt = LocalDateTime.now().minusHours(8); calculatedSoldCount = 310; }
            else if (idx == 61) { assignedCreatedAt = LocalDateTime.now().minusHours(10); calculatedSoldCount = 490; }
            else if (idx == 79) { assignedCreatedAt = LocalDateTime.now().minusHours(12); calculatedSoldCount = 280; }
            else if (idx == 92) { assignedCreatedAt = LocalDateTime.now().minusHours(14); calculatedSoldCount = 360; }
            else if (idx == 107) { assignedCreatedAt = LocalDateTime.now().minusHours(16); calculatedSoldCount = 550; }
            else if (idx == 5) { assignedCreatedAt = LocalDateTime.now().minusHours(18); calculatedSoldCount = 410; }
            else if (idx == 7) { assignedCreatedAt = LocalDateTime.now().minusHours(20); calculatedSoldCount = 220; }
            else if (idx == 11) { assignedCreatedAt = LocalDateTime.now().minusHours(22); calculatedSoldCount = 580; }
            else if (idx == 82) { assignedCreatedAt = LocalDateTime.now().minusHours(24); calculatedSoldCount = 390; }
            else {
                calculatedSoldCount = Math.max(80, (idx * 17) % 600 + 100);
            }

            product.setAverageRating(assignedRating);
            product.setSoldCount(calculatedSoldCount);
            product.setCreatedAt(assignedCreatedAt);
            product.setUpdatedAt(assignedCreatedAt);
            product.setSlug(toSlug(item.name) + "-" + UUID.randomUUID().toString().substring(0, 8));

            product = productRepository.save(product);

            if (item.images != null && !item.images.isEmpty()) {
                List<ProductImage> images = new ArrayList<>();
                for (int i = 0; i < item.images.size(); i++) {
                    String url = item.images.get(i).trim();
                    if (url.isEmpty()) continue;
                    ProductImage img = new ProductImage();
                    img.setImageUrl(url);
                    img.setPrimary(i == 0);
                    img.setDisplayOrder(i);
                    img.setProduct(product);
                    images.add(img);
                }
                productImageRepository.saveAll(images);
            }

            savedProducts.add(product);
        }

        log.info("Successfully seeded {} curated products (15 per category across 8 categories)!", savedProducts.size());

        // 5. Seed 8 Active Flash Sales (Deals of the Day)
        List<Integer> flashDealIndices = List.of(8, 19, 35, 48, 62, 77, 93, 108);
        List<BigDecimal> flashPrices = List.of(
            BigDecimal.valueOf(6999.00),  // JBL Flip 6
            BigDecimal.valueOf(4999.00),  // Ray-Ban Aviator
            BigDecimal.valueOf(799.00),   // Milton Flask
            BigDecimal.valueOf(399.00),   // Nutella
            BigDecimal.valueOf(349.00),   // Maybelline Lipstick
            BigDecimal.valueOf(1299.00),  // Speedo Goggles
            BigDecimal.valueOf(799.00),   // Hasbro Monopoly
            BigDecimal.valueOf(299.00)    // Sapiens Book
        );

        List<FlashSale> flashSales = new ArrayList<>();
        for (int i = 0; i < flashDealIndices.size(); i++) {
            int targetIdx = flashDealIndices.get(i);
            if (targetIdx < savedProducts.size()) {
                Product targetProduct = savedProducts.get(targetIdx);
                FlashSale fs = new FlashSale();
                fs.setProduct(targetProduct);
                fs.setSalePrice(flashPrices.get(i));
                fs.setStartsAt(LocalDateTime.now().minusDays(1));
                fs.setEndsAt(LocalDateTime.now().plusDays(2));
                fs.setStockLimit(50);
                fs.setSoldCount(18);
                flashSales.add(fs);
            }
        }
        flashSaleRepository.saveAll(flashSales);
        log.info("Successfully seeded {} active Flash Sales for Deals of the Day!", flashSales.size());

        // 6. Re-index VectorStore with all 120 new products
        try {
            int vectorCount = productVectorSyncService.syncAllProducts();
            log.info("VectorStore catalog synced with {} curated products.", vectorCount);
        } catch (Exception ex) {
            log.warn("Vector sync after seeding: {}", ex.getMessage());
        }
    }

    private String toSlug(String input) {
        if (input == null) return "";
        return input.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-$", "").replaceAll("^-", "");
    }
}
