package com.swiftcart.seeder;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.swiftcart.entity.Category;
import com.swiftcart.entity.Product;
import com.swiftcart.entity.ProductImage;
import com.swiftcart.entity.User;
import com.swiftcart.enums.Role;
import com.swiftcart.repository.CategoryRepository;
import com.swiftcart.repository.ProductImageRepository;
import com.swiftcart.repository.ProductRepository;
import com.swiftcart.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;


@Component
@Profile("dev")
public class DatabaseSeeder implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;

    private static final String[] ROOT_CATEGORIES = {"Electronics", "Fashion", "Home", "Grocery", "Beauty", "Sports", "Toys", "Books"};
    
    private static final Map<String, String> SUBCAT_TO_ROOT = new HashMap<>();
    static {
        SUBCAT_TO_ROOT.put("Accessories", "Home");
        SUBCAT_TO_ROOT.put("Bags", "Fashion");
        SUBCAT_TO_ROOT.put("Camera Accessories", "Electronics");
        SUBCAT_TO_ROOT.put("College Supplies", "Books");
        SUBCAT_TO_ROOT.put("Laptop Accessories", "Electronics");
        SUBCAT_TO_ROOT.put("Mobile Accessories", "Electronics");
        // We will default anything unknown to "Home" just like the python script
    }

    public DatabaseSeeder(CategoryRepository categoryRepository, ProductRepository productRepository, ProductImageRepository productImageRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0) {
            log.info("Database already seeded. Skipping...");
            return;
        }
        
        log.info("Starting database seed from CSV...");
        
        // 1. Create a dummy Seller User if one doesn't exist
        User defaultSeller = userRepository.findByEmail("seller_seed@swiftcart.com").orElseGet(() -> {
            User seller = User.builder()
                .name("Seed Seller")
                .email("seller_seed@swiftcart.com")
                .passwordHash("seed123")
                .role(Role.SELLER)
                .phone("9999999999")
                .isVerified(true)
                .build();
            return userRepository.save(seller);
        });

        // 2. Insert Root Categories
        Map<String, Category> rootCategories = new HashMap<>();
        for (int i = 0; i < ROOT_CATEGORIES.length; i++) {
            String rootName = ROOT_CATEGORIES[i];
            Category root = new Category();
            root.setName(rootName);
            root.setSlug(toSlug(rootName));
            root.setActive(true);
            root.setDisplayOrder(i);
            root = categoryRepository.save(root);
            rootCategories.put(rootName, root);
        }

        // 3. Process CSV
        ClassPathResource resource = new ClassPathResource("data/flipkart_products_formatted.csv");
        if (!resource.exists()) {
            log.warn("Seed file flipkart_products_formatted.csv not found in classpath. Seeding aborted.");
            return;
        }

        Map<String, Category> subCategories = new HashMap<>();

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(resource.getInputStream())).withSkipLines(1).build()) {
            String[] line;
            int prodCount = 0;
            while ((line = reader.readNext()) != null) {
                if (line.length < 8) continue;
                
                String name = line[0];
                String brand = line[1];
                String subcatName = line[2];
                String basePriceStr = line[3];
                String mrpStr = line[4];
                String stockQtyStr = line[5];
                String desc = line[6];
                String imageUrlList = line[7];

                // Resolve root category
                String rootName = SUBCAT_TO_ROOT.getOrDefault(subcatName, "Home");
                Category parent = rootCategories.get(rootName);

                // Find or create subcategory
                Category subCat = subCategories.computeIfAbsent(subcatName, k -> {
                    Category cat = new Category();
                    cat.setName(subcatName);
                    cat.setSlug(toSlug(subcatName) + "-" + UUID.randomUUID().toString().substring(0, 5));
                    cat.setParent(parent);
                    cat.setActive(true);
                    return categoryRepository.save(cat);
                });

                // Create Product
                Product product = new Product();
                product.setName(name);
                product.setBrand(brand);
                product.setDescription(desc);
                product.setBasePrice(new BigDecimal(basePriceStr));
                product.setMrp(new BigDecimal(mrpStr));
                product.setStockQty(Integer.parseInt(stockQtyStr));
                product.setCategory(subCat);
                product.setSeller(defaultSeller);
                product.setSlug(toSlug(name) + "-" + UUID.randomUUID().toString().substring(0, 8));
                product.setActive(true);
                product.setAverageRating(new BigDecimal("4.2"));
                
                product = productRepository.save(product);

                // Create Images
                if (imageUrlList != null && !imageUrlList.isEmpty()) {
                    String[] urls = imageUrlList.split("\\|");
                    List<ProductImage> images = new ArrayList<>();
                    for (int i = 0; i < urls.length; i++) {
                        String url = urls[i].trim();
                        if (url.isEmpty()) continue;
                        ProductImage image = new ProductImage();
                        image.setImageUrl(url);
                        image.setPrimary(i == 0);
                        image.setDisplayOrder(i);
                        image.setProduct(product);
                        images.add(image);
                    }
                    productImageRepository.saveAll(images);
                }
                
                prodCount++;
                if (prodCount % 100 == 0) {
                    log.info("Seeded {} products...", prodCount);
                }
            }
            log.info("Database seeding completed successfully! {} products inserted.", prodCount);
        } catch (CsvValidationException e) {
            log.error("Error parsing seed CSV", e);
        }
    }

    private String toSlug(String input) {
        if (input == null) return "";
        return input.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-$", "").replaceAll("^-", "");
    }
}
