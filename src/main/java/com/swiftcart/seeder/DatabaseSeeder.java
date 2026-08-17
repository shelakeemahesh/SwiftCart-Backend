package com.swiftcart.seeder;

import com.swiftcart.entity.Category;
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
        boolean needsReSeed = (currentCount != 120);

        if (!needsReSeed) {
            Optional<Product> sample = productRepository.findAll().stream().findFirst();
            if (sample.isPresent() && (sample.get().getSoldCount() == 0 || !sample.get().getName().contains("Apple") && !sample.get().getName().contains("iPhone") && !sample.get().getName().contains("Sony"))) {
                needsReSeed = true;
            }
        }

        if (!needsReSeed) {
            log.info("Database already seeded with 120 curated products. Skipping product catalog seeder...");
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
        int seededCount = 0;

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
            product.setAverageRating(item.rating);
            product.setReviewCount(item.reviewCount);
            product.setHighlights(item.highlights);
            product.setSpecifications(item.specifications);
            product.setDescription(item.description);
            product.setSeller(defaultSeller);
            product.setActive(true);
            product.setFeatured(idx % 3 == 0);
            
            // Calculate realistic sold count and staggered creation date
            // Items at beginning of category list (latest flagships) have fresh createdAt
            int categoryItemIndex = idx % 15;
            int daysAgo = categoryItemIndex == 0 ? 1 : (categoryItemIndex == 1 ? 2 : (categoryItemIndex * 2 + 1));
            int calculatedSoldCount = Math.max(50, (int) (item.reviewCount * 3.5) + (15 - categoryItemIndex) * 40);
            
            product.setSoldCount(calculatedSoldCount);
            product.setCreatedAt(LocalDateTime.now().minusDays(daysAgo).minusHours(idx % 12));
            product.setUpdatedAt(LocalDateTime.now().minusDays(daysAgo / 2));
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

            seededCount++;
        }

        log.info("Successfully seeded {} curated products (15 per category across 8 categories)!", seededCount);

        // 5. Re-index VectorStore with all 120 new products
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
