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
@Profile({"dev", "prod"})
public class DatabaseSeeder implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;

    private static final String[] ROOT_CATEGORIES = {"Electronics", "Fashion", "Home", "Grocery", "Beauty", "Sports", "Toys", "Books"};
    
    private static final Map<String, String> SUBCAT_TO_ROOT = new HashMap<>();
    static {
        // Electronics
        SUBCAT_TO_ROOT.put("Camera Accessories", "Electronics");
        SUBCAT_TO_ROOT.put("Laptop Accessories", "Electronics");
        SUBCAT_TO_ROOT.put("Mobile Accessories", "Electronics");
        SUBCAT_TO_ROOT.put("Tablet Accessories", "Electronics");
        SUBCAT_TO_ROOT.put("Computer Peripherals", "Electronics");
        SUBCAT_TO_ROOT.put("Network Components", "Electronics");
        SUBCAT_TO_ROOT.put("NEWGEN TECH EO-HS3303 218 Wired Headset (White)", "Electronics");

        // Fashion
        SUBCAT_TO_ROOT.put("Bags", "Fashion");
        SUBCAT_TO_ROOT.put("Men's Clothing", "Fashion");
        SUBCAT_TO_ROOT.put("Women's Clothing", "Fashion");
        SUBCAT_TO_ROOT.put("Men's Footwear", "Fashion");
        SUBCAT_TO_ROOT.put("Women's Footwear", "Fashion");
        SUBCAT_TO_ROOT.put("Belts", "Fashion");
        SUBCAT_TO_ROOT.put("Wrist Watches", "Fashion");
        SUBCAT_TO_ROOT.put("Watch Accessories", "Fashion");
        SUBCAT_TO_ROOT.put("Bangles, Bracelets & Armlets", "Fashion");
        SUBCAT_TO_ROOT.put("Necklaces & Chains", "Fashion");
        SUBCAT_TO_ROOT.put("Jewellery Sets", "Fashion");
        SUBCAT_TO_ROOT.put("Pendants & Lockets", "Fashion");
        SUBCAT_TO_ROOT.put("Rings", "Fashion");
        SUBCAT_TO_ROOT.put("HH Oval Sunglasses", "Fashion");
        SUBCAT_TO_ROOT.put("Olvin Aviator Sunglasses", "Fashion");
        SUBCAT_TO_ROOT.put("Clovia Women's Full Coverage Bra", "Fashion");
        SUBCAT_TO_ROOT.put("Leading lady Women's Camisole", "Fashion");
        SUBCAT_TO_ROOT.put("RajeshFashion Women's Leggings", "Fashion");
        SUBCAT_TO_ROOT.put("Siyas Collection Lac Cubic Zirconia Bangle Set (...", "Fashion");
        SUBCAT_TO_ROOT.put("TSG Breeze Printed Women's Round Neck Multicolor...", "Fashion");
        SUBCAT_TO_ROOT.put("Vishudh Printed Women's Straight Kurta", "Fashion");
        SUBCAT_TO_ROOT.put("soie Fashion Women's Full Coverage Bra", "Fashion");
        SUBCAT_TO_ROOT.put("Breakbounce Men's Vest", "Fashion");
        SUBCAT_TO_ROOT.put("Klaur Melbourne Bellies", "Fashion");

        // Toys
        SUBCAT_TO_ROOT.put("Action Figures", "Toys");
        SUBCAT_TO_ROOT.put("Baby & Kids Gifts", "Toys");
        SUBCAT_TO_ROOT.put("Diapering & Potty Training", "Toys");
        SUBCAT_TO_ROOT.put("Infant Wear", "Toys");
        SUBCAT_TO_ROOT.put("Kids' Clothing", "Toys");

        // Books
        SUBCAT_TO_ROOT.put("College Supplies", "Books");
        SUBCAT_TO_ROOT.put("Pens", "Books");
        SUBCAT_TO_ROOT.put("School Supplies", "Books");

        // Beauty
        SUBCAT_TO_ROOT.put("Body and Skin Care", "Beauty");
        SUBCAT_TO_ROOT.put("Hair Care", "Beauty");
        SUBCAT_TO_ROOT.put("Makeup", "Beauty");
        SUBCAT_TO_ROOT.put("Fragrances", "Beauty");
        SUBCAT_TO_ROOT.put("Personal Care Appliances", "Beauty");

        // Sports
        SUBCAT_TO_ROOT.put("Outdoor & Adventure", "Sports");
        SUBCAT_TO_ROOT.put("Car Accessories", "Sports");
        SUBCAT_TO_ROOT.put("Car & Bike Accessories", "Sports");
        SUBCAT_TO_ROOT.put("Gking Hand Stiched Steering Cover For Maruti Ert...", "Sports");

        // Grocery
        SUBCAT_TO_ROOT.put("Combos and Kits", "Grocery");
        SUBCAT_TO_ROOT.put("Health Care", "Grocery");
        SUBCAT_TO_ROOT.put("Housekeeping & Laundry", "Grocery");

        // Home
        SUBCAT_TO_ROOT.put("Accessories", "Home");
        SUBCAT_TO_ROOT.put("Accessories & Spare parts", "Home");
        SUBCAT_TO_ROOT.put("Bar & Glassware", "Home");
        SUBCAT_TO_ROOT.put("Bath Linen", "Home");
        SUBCAT_TO_ROOT.put("Bed Linen", "Home");
        SUBCAT_TO_ROOT.put("Bedroom Furniture", "Home");
        SUBCAT_TO_ROOT.put("Coffee Mugs", "Home");
        SUBCAT_TO_ROOT.put("Cookware", "Home");
        SUBCAT_TO_ROOT.put("Curtains & Accessories", "Home");
        SUBCAT_TO_ROOT.put("Cushions, Pillows & Covers", "Home");
        SUBCAT_TO_ROOT.put("Dinnerware & Crockery", "Home");
        SUBCAT_TO_ROOT.put("Festive Decor", "Home");
        SUBCAT_TO_ROOT.put("Floor Coverings", "Home");
        SUBCAT_TO_ROOT.put("Kitchen & Dining Linen", "Home");
        SUBCAT_TO_ROOT.put("Kitchen Appliances", "Home");
        SUBCAT_TO_ROOT.put("Kitchen Tools", "Home");
        SUBCAT_TO_ROOT.put("Lighting", "Home");
        SUBCAT_TO_ROOT.put("Living Room Furnishing", "Home");
        SUBCAT_TO_ROOT.put("Pet Furniture", "Home");
        SUBCAT_TO_ROOT.put("Showpiece", "Home");
        SUBCAT_TO_ROOT.put("Showpieces", "Home");
        SUBCAT_TO_ROOT.put("Table Decor & Handicrafts", "Home");
        SUBCAT_TO_ROOT.put("Tools", "Home");
        SUBCAT_TO_ROOT.put("Wall Decor & Clocks", "Home");
        SUBCAT_TO_ROOT.put("Candles & Fragrances", "Home");
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
        if (productRepository.count() > 0) {
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
            final int displayOrder = i;
            Category root = categoryRepository.findByName(rootName).orElseGet(() -> {
                Category cat = new Category();
                cat.setName(rootName);
                cat.setSlug(toSlug(rootName));
                cat.setActive(true);
                cat.setDisplayOrder(displayOrder);
                return categoryRepository.save(cat);
            });
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
                    return categoryRepository.findByNameAndParent(subcatName, parent).orElseGet(() -> {
                        Category cat = new Category();
                        cat.setName(subcatName);
                        cat.setSlug(toSlug(subcatName) + "-" + UUID.randomUUID().toString().substring(0, 5));
                        cat.setParent(parent);
                        cat.setActive(true);
                        return categoryRepository.save(cat);
                    });
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
