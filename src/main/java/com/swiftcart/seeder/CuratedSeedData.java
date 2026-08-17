package com.swiftcart.seeder;

import java.math.BigDecimal;
import java.util.*;

public class CuratedSeedData {

    public static class SeedItem {
        public String name;
        public String brand;
        public String category; // One of the 8 Root Categories
        public BigDecimal price;
        public BigDecimal mrp;
        public int discountPercent;
        public int stockQty;
        public BigDecimal rating;
        public int reviewCount;
        public List<String> images;
        public List<String> highlights;
        public Map<String, String> specifications;
        public String description;

        public SeedItem(
                String name,
                String brand,
                String category,
                double price,
                double mrp,
                int stockQty,
                double rating,
                int reviewCount,
                List<String> images,
                List<String> highlights,
                Map<String, String> specifications,
                String description) {
            this.name = name;
            this.brand = brand;
            this.category = category;
            this.price = BigDecimal.valueOf(price);
            this.mrp = BigDecimal.valueOf(mrp);
            this.discountPercent = (int) Math.round(((mrp - price) / mrp) * 100);
            this.stockQty = stockQty;
            this.rating = BigDecimal.valueOf(rating);
            this.reviewCount = reviewCount;
            this.images = images;
            this.highlights = highlights;
            this.specifications = specifications;
            this.description = description;
        }
    }

    public static List<SeedItem> getCuratedProducts() {
        List<SeedItem> list = new ArrayList<>();

        // ==========================================
        // 1. ELECTRONICS (15 PRODUCTS)
        // ==========================================
        list.add(new SeedItem(
            "Apple iPhone 15 Pro (128 GB) - Natural Titanium",
            "Apple", "Electronics", 127990, 134900, 45, 4.8, 342,
            List.of(
                "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800",
                "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=800"
            ),
            List.of("A17 Pro chip with 6-core GPU", "Super Retina XDR OLED Display with ProMotion", "48MP Main camera with 3x Telephoto", "Aerospace-grade titanium design"),
            Map.of("Display", "6.1 inch Super Retina XDR", "Processor", "A17 Pro", "Storage", "128 GB", "Camera", "48MP + 12MP + 12MP"),
            "iPhone 15 Pro forged in titanium and featuring the groundbreaking A17 Pro chip, a customizable Action button, and the most powerful iPhone camera system ever."
        ));

        list.add(new SeedItem(
            "Apple MacBook Pro 14-inch M3 Pro (18GB / 512GB SSD)",
            "Apple", "Electronics", 199900, 209900, 25, 4.9, 189,
            List.of(
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800",
                "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800"
            ),
            List.of("Apple M3 Pro 11-core CPU", "Liquid Retina XDR display with 1000 nits sustained brightness", "Up to 18 hours battery life", "MagSafe 3 and Thunderbolt 4 ports"),
            Map.of("RAM", "18 GB Unified Memory", "Storage", "512 GB SSD", "Display", "14.2 inch Liquid Retina XDR", "OS", "macOS Sonoma"),
            "Supercharged by M3 Pro, MacBook Pro takes power and efficiency to new heights with extreme speed for demanding workflows."
        ));

        list.add(new SeedItem(
            "Sony WH-1000XM5 Wireless Noise Cancelling Headphones",
            "Sony", "Electronics", 29990, 34990, 60, 4.7, 512,
            List.of(
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800",
                "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=800"
            ),
            List.of("Industry-leading Active Noise Cancellation", "30-hour battery life with quick charging", "Crystal-clear hands-free calling with 4 beamforming mics", "Multipoint connection for two devices"),
            Map.of("Battery Life", "30 Hours", "Driver Unit", "30mm Carbon Fiber", "Connectivity", "Bluetooth 5.2 & 3.5mm", "Weight", "250g"),
            "Experience pure silence and pristine Hi-Res audio with Sony's flagship WH-1000XM5 noise cancelling headphones."
        ));

        list.add(new SeedItem(
            "Apple Watch Series 9 GPS 45mm - Midnight Aluminum",
            "Apple", "Electronics", 44900, 47900, 40, 4.8, 275,
            List.of(
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800",
                "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=800"
            ),
            List.of("S9 SiP chip with Double Tap gesture", "Always-On Retina display with 2000 nits", "ECG and Blood Oxygen app", "Crash Detection and Emergency SOS"),
            Map.of("Case Size", "45mm", "Connectivity", "GPS & Bluetooth 5.3", "Water Resistance", "50 meters", "Sensors", "ECG, SpO2, Heart Rate"),
            "Smarter, brighter, and mightier. Apple Watch Series 9 helps you stay connected, active, healthy, and safe."
        ));

        list.add(new SeedItem(
            "Samsung Galaxy S24 Ultra 5G (256 GB / 12 GB RAM)",
            "Samsung", "Electronics", 129999, 134999, 30, 4.7, 210,
            List.of(
                "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800",
                "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800"
            ),
            List.of("Galaxy AI built-in features", "200MP Quad Telephoto Camera", "Titanium Frame with Gorilla Glass Armor", "Embedded S Pen stylus"),
            Map.of("Processor", "Snapdragon 8 Gen 3", "Display", "6.8 inch Dynamic AMOLED 2X 120Hz", "Battery", "5000 mAh", "RAM", "12 GB"),
            "Unleash new levels of creativity, productivity and possibility starting with the most important device in your life."
        ));

        list.add(new SeedItem(
            "Apple iPad Air 11-inch M2 (128 GB Wi-Fi)",
            "Apple", "Electronics", 59900, 62900, 50, 4.8, 140,
            List.of(
                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800",
                "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=800"
            ),
            List.of("Blazing-fast M2 chip performance", "Liquid Retina display with True Tone & P3 wide color", "Supports Apple Pencil Pro and Magic Keyboard", "Landscape 12MP Ultra Wide front camera"),
            Map.of("Display", "11.0 inch Liquid Retina", "Processor", "Apple M2", "Storage", "128 GB", "Weight", "462g"),
            "Freshly redesigned, the new iPad Air is charged with extraordinary speed from Apple's M2 chip."
        ));

        list.add(new SeedItem(
            "Sony PlayStation 5 Digital Edition Console (Slim)",
            "Sony", "Electronics", 44990, 49990, 20, 4.9, 450,
            List.of(
                "https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=800",
                "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800"
            ),
            List.of("1TB Custom Ultra-High Speed SSD", "DualSense Wireless Controller with Haptic Feedback", "Tempest 3D AudioTech & Ray Tracing", "4K 120Hz and 8K HDR support"),
            Map.of("Storage", "1 TB Custom NVMe SSD", "Resolution", "Up to 4K 120fps", "Audio", "Tempest 3D Audio", "Color", "White/Black"),
            "Experience lightning-fast loading with an ultra-high speed SSD and deeper immersion with haptic feedback and 3D Audio."
        ));

        list.add(new SeedItem(
            "Canon EOS R6 Mark II 24.2MP Mirrorless Camera (Body Only)",
            "Canon", "Electronics", 215990, 243995, 15, 4.9, 85,
            List.of(
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800",
                "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800"
            ),
            List.of("24.2 MP Full-Frame CMOS Sensor", "40 fps Electronic Shutter Burst Shooting", "6K oversampled 4K 60p full-width video", "In-Body Image Stabilizer up to 8 stops"),
            Map.of("Sensor", "Full-Frame 24.2MP", "Video", "4K 60fps / 6K Raw", "Mount", "Canon RF Mount", "ISO Range", "100-102400"),
            "Master both high-resolution still photography and cinematic filmmaking with the versatile EOS R6 Mark II."
        ));

        list.add(new SeedItem(
            "JBL Flip 6 Portable Waterproof Bluetooth Speaker",
            "JBL", "Electronics", 9999, 13999, 75, 4.6, 620,
            List.of(
                "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800",
                "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=800"
            ),
            List.of("JBL Original Pro Sound with 2-way speaker system", "IP67 Waterproof and Dustproof", "12 Hours of Playtime on a single charge", "PartyBoost for pairing multiple speakers"),
            Map.of("Output", "30W RMS", "Battery Life", "12 Hours", "Waterproof Rating", "IP67", "Bluetooth", "v5.1"),
            "Bold sound for every adventure. The 2-way speaker system delivers loud, crystal clear, powerful sound."
        ));

        list.add(new SeedItem(
            "Dell XPS 15 9530 Core i7 13th Gen (16GB / 1TB SSD / RTX 4050)",
            "Dell", "Electronics", 189990, 214990, 18, 4.7, 95,
            List.of(
                "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800",
                "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800"
            ),
            List.of("Intel Core i7-13700H 14-Core Processor", "NVIDIA GeForce RTX 4050 6GB GDDR6", "15.6 inch FHD+ InfinityEdge 500 nits Display", "CNC machined aluminum & carbon fiber chassis"),
            Map.of("CPU", "Intel Core i7-13700H", "GPU", "NVIDIA RTX 4050", "RAM", "16 GB DDR5", "Storage", "1 TB NVMe SSD"),
            "Immerse yourself in content with stunning panels, rich color, and incredible performance for creators."
        ));

        list.add(new SeedItem(
            "Bose QuietComfort 45 Bluetooth Wireless Headphones",
            "Bose", "Electronics", 26900, 29900, 35, 4.6, 310,
            List.of(
                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=800",
                "https://images.unsplash.com/photo-1572536147248-ac59a8abfa4b?w=800"
            ),
            List.of("Iconic Quiet and Aware noise cancellation modes", "High-fidelity audio with adjustable EQ", "Up to 24 hours battery life", "Plush synthetic leather earcups"),
            Map.of("Battery Life", "24 Hours", "Charging", "USB-C Quick Charge", "Weight", "240g", "Microphone", "Quad-mic system"),
            "The perfect balance of quiet, comfort, and sound. Bose QuietComfort 45 delivers the ultimate audio immersion."
        ));

        list.add(new SeedItem(
            "Nintendo Switch OLED Model - White Joy-Con",
            "Nintendo", "Electronics", 31990, 37990, 45, 4.8, 410,
            List.of(
                "https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?w=800",
                "https://images.unsplash.com/photo-1612287233207-63a2a3fa41f1?w=800"
            ),
            List.of("Vibrant 7-inch OLED screen with vivid colors", "Wide adjustable stand for tabletop mode", "Wired LAN port in dock for stable online play", "64 GB internal storage"),
            Map.of("Screen", "7.0 inch OLED 720p (1080p Docked)", "Storage", "64 GB", "Battery Life", "4.5 to 9 Hours", "Modes", "TV, Tabletop, Handheld"),
            "Feast your eyes on vivid colors and crisp contrast when you play on-the-go with the 7-inch OLED screen."
        ));

        list.add(new SeedItem(
            "GoPro HERO12 Black Waterproof 5.3K Action Camera",
            "GoPro", "Electronics", 37990, 45000, 30, 4.7, 180,
            List.of(
                "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800",
                "https://images.unsplash.com/photo-1500485035595-cbe6f645feb1?w=800"
            ),
            List.of("5.3K60 + 4K120 resolution video recording", "HyperSmooth 6.0 video stabilization with AutoBoost", "Rugged and waterproof up to 33ft (10m)", "HDR photo and video with wireless audio support"),
            Map.of("Video Resolution", "5.3K 60fps, 4K 120fps", "Photo", "27 MP", "Waterproof", "10m without housing", "Battery", "Enduro 1720mAh"),
            "Incredible image quality, even better HyperSmooth video stabilization and a huge boost in battery life."
        ));

        list.add(new SeedItem(
            "Logitech MX Master 3S Performance Wireless Mouse",
            "Logitech", "Electronics", 9495, 10995, 80, 4.8, 650,
            List.of(
                "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=800",
                "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=800"
            ),
            List.of("8K DPI any-surface optical sensor (tracks on glass)", "Quiet Clicks with 90% less click noise", "MagSpeed electromagnetic scrolling", "Ergonomic silhouette with thumb wheel"),
            Map.of("DPI", "8000 DPI", "Battery", "Up to 70 days", "Connectivity", "Bluetooth & Logi Bolt", "Weight", "141g"),
            "An icon remastered. Feel every moment of your workflow with even more precision, tactility, and performance."
        ));

        list.add(new SeedItem(
            "LG C3 55-inch 4K OLED evo Smart TV (OLED55C3)",
            "LG", "Electronics", 124990, 169990, 15, 4.9, 130,
            List.of(
                "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=800",
                "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=800"
            ),
            List.of("Self-lit OLED pixels with Infinite Contrast", "a9 AI Processor Gen6 with AI Super Upscaling", "0.1ms response time with G-Sync & FreeSync for Gaming", "Dolby Vision & Dolby Atmos built-in"),
            Map.of("Display Size", "55 inch 4K Ultra HD", "Refresh Rate", "120 Hz Native", "HDR", "Dolby Vision, HDR10", "Ports", "4x HDMI 2.1"),
            "The world's number one OLED brand brings bright, vibrant beauty with the advanced a9 AI Processor Gen6."
        ));


        // ==========================================
        // 2. FASHION (15 PRODUCTS)
        // ==========================================
        list.add(new SeedItem(
            "Levi's Men's 511 Slim Fit Premium Stretch Jeans",
            "Levi's", "Fashion", 2499, 4199, 65, 4.6, 420,
            List.of(
                "https://images.unsplash.com/photo-1542272604-780c96856592?w=800",
                "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800"
            ),
            List.of("Modern slim fit with room to move", "Added stretch for all-day comfort", "Classic 5-pocket styling", "Durable denim construction"),
            Map.of("Material", "99% Cotton, 1% Elastane", "Fit", "Slim Fit", "Rise", "Mid Rise", "Wash", "Medium Indigo"),
            "A modern slim with room to move, the 511 Slim Fit Stretch Jeans are a classic since right now."
        ));

        list.add(new SeedItem(
            "Zara Floral Print Summer Tiered Maxi Dress",
            "Zara", "Fashion", 3590, 4990, 40, 4.7, 215,
            List.of(
                "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=800",
                "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=800"
            ),
            List.of("Lightweight breathable chiffon fabric", "V-neck with delicate flutter sleeves", "Tiered flowing skirt with side slit", "Elasticated smocked waist"),
            Map.of("Material", "100% Viscose", "Length", "Maxi Length", "Neckline", "V-Neck", "Occasion", "Casual / Resort Wear"),
            "Effortlessly chic and airy, this floral tiered maxi dress is your ideal choice for summer gatherings and vacations."
        ));

        list.add(new SeedItem(
            "Tommy Hilfiger Men's Classic Pique Cotton Polo Shirt",
            "Tommy Hilfiger", "Fashion", 2999, 4599, 70, 4.7, 310,
            List.of(
                "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800",
                "https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=800"
            ),
            List.of("100% Organic Pique Cotton fabric", "Signature embroidered flag logo on chest", "Ribbed collar and two-button placket", "Custom regular fit"),
            Map.of("Material", "100% Organic Cotton", "Fit", "Regular Fit", "Sleeve", "Short Sleeve", "Care", "Machine Wash"),
            "The timeless classic polo crafted in breathable pique cotton with iconic Tommy Hilfiger detailing."
        ));

        list.add(new SeedItem(
            "Nike Air Max 270 Men's Breathable Running Shoes",
            "Nike", "Fashion", 9995, 13995, 50, 4.8, 560,
            List.of(
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800",
                "https://images.unsplash.com/photo-1552346154-21d32810aba3?w=800"
            ),
            List.of("Large Max Air 270 unit delivers all-day cushion", "Engineered mesh upper for lightweight breathability", "Stretchy inner sleeve creates a snug fit", "Durable rubber outsole with traction grooves"),
            Map.of("Sole Material", "Rubber with Max Air Cushion", "Closure", "Lace-Up", "Weight", "310g", "Type", "Lifestyle / Running"),
            "Nike's first lifestyle Air Max brings you style, comfort and big attitude with every stride."
        ));

        list.add(new SeedItem(
            "Ray-Ban Classic Polarized Aviator Sunglasses (Gold/Green)",
            "Ray-Ban", "Fashion", 7990, 9590, 35, 4.9, 390,
            List.of(
                "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=800",
                "https://images.unsplash.com/photo-1577803645773-f96470509666?w=800"
            ),
            List.of("100% UV400 polarized crystal lenses", "Classic teardrop gold metal frame", "Adjustable silicone nose pads", "Includes authentic leather protective case"),
            Map.of("Frame Material", "Metal", "Lens Width", "58 mm", "Lens Type", "Polarized G-15", "Gender", "Unisex"),
            "Originally designed for U.S. Aviators in 1937, Ray-Ban Aviator sunglasses combine great styling with exceptional quality."
        ));

        list.add(new SeedItem(
            "Fossil Grant Chronograph Brown Leather Watch (FS4813)",
            "Fossil", "Fashion", 8995, 12495, 45, 4.7, 280,
            List.of(
                "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=800",
                "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=800"
            ),
            List.of("Classic Roman numeral hour markers", "Genuine rich brown leather strap", "Precision Quartz chronograph movement with 3 sub-dials", "50m water resistance"),
            Map.of("Case Diameter", "44 mm", "Band Material", "Genuine Leather", "Movement", "Quartz Chronograph", "Water Resistance", "5 ATM"),
            "Modeled after vintage clocks, Roman numerals add classic style to the Grant chronograph watch."
        ));

        list.add(new SeedItem(
            "Adidas Originals SST Track Jacket - Black & White",
            "Adidas", "Fashion", 3999, 5999, 55, 4.7, 340,
            List.of(
                "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=800",
                "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800"
            ),
            List.of("Iconic 3-Stripes along the sleeves", "Full two-way front zip with ribbed collar", "Side zip pockets for secure storage", "Recycled polyester blend tricot fabric"),
            Map.of("Material", "70% Recycled Polyester, 30% Cotton", "Fit", "Regular Fit", "Neck", "Ribbed Stand-Up Collar", "Gender", "Men"),
            "A streetwear staple since the 70s. The iconic SST track jacket keeps your sporty look sharp and comfortable."
        ));

        list.add(new SeedItem(
            "H&M Premium 100% Pure Linen Long Sleeve Shirt",
            "H&M", "Fashion", 1999, 2999, 60, 4.5, 190,
            List.of(
                "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800",
                "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800"
            ),
            List.of("100% pure premium airy woven linen", "Regular fit with a classic turn-down collar", "Curved hemline with single chest pocket", "Breathable natural fabric perfect for warm climates"),
            Map.of("Material", "100% Linen", "Fit", "Regular Fit", "Sleeve Length", "Long Sleeve", "Color", "Sky Blue"),
            "Crafted from premium pure linen, this versatile shirt provides an effortlessly crisp yet relaxed silhouette."
        ));

        list.add(new SeedItem(
            "Puma Suede Classic XXI Low-Top Leather Sneakers",
            "Puma", "Fashion", 4199, 6999, 50, 4.6, 260,
            List.of(
                "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800",
                "https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=800"
            ),
            List.of("Full premium velvet suede upper", "Comfort sockliner for instant step-in cushioning", "Rubber midsole and outsole for maximum grip", "Metallic gold foil Puma branding"),
            Map.of("Upper", "Suede Leather", "Sole", "Durable Rubber", "Closure", "Lace-Up", "Style", "Heritage Classic"),
            "The Suede hit the scene in 1968 and has been changing the footwear game ever since."
        ));

        list.add(new SeedItem(
            "Manyavar Royal Silk Blend Jacquard Kurta Pajama Set",
            "Manyavar", "Fashion", 4999, 7999, 30, 4.8, 175,
            List.of(
                "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=800",
                "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=800"
            ),
            List.of("Rich silk blend with woven self-jacquard pattern", "Mandarin collar with ornate metallic button detail", "Includes matching ivory churidar pajama", "Ideal for weddings and festive celebrations"),
            Map.of("Fabric", "Art Silk Jacquard", "Collar", "Mandarin Collar", "Length", "Knee Length", "Occasion", "Festive / Wedding"),
            "Drape yourself in royal elegance with Manyavar's finely crafted jacquard silk kurta pajama set."
        ));

        list.add(new SeedItem(
            "Vero Moda High-Waist Wide-Leg Tailored Trousers",
            "Vero Moda", "Fashion", 2199, 3499, 45, 4.6, 140,
            List.of(
                "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=800",
                "https://images.unsplash.com/photo-1551803091-e20673f15770?w=800"
            ),
            List.of("Flattering high-rise waist with pleated front", "Wide-leg silhouette for effortless movement", "Dual functional side slash pockets", "Premium crepe stretch fabric"),
            Map.of("Material", "78% Polyester, 18% Viscose, 4% Elastane", "Fit", "Wide Leg", "Rise", "High Rise", "Closure", "Zip Fly with Hook"),
            "Sophisticated, versatile trousers designed to take you effortlessly from boardrooms to weekend brunches."
        ));

        list.add(new SeedItem(
            "Wildhorn Genuine Leather Bifold Wallet for Men",
            "Wildhorn", "Fashion", 999, 1999, 90, 4.7, 680,
            List.of(
                "https://images.unsplash.com/photo-1627123424574-724758594e93?w=800",
                "https://images.unsplash.com/photo-1606760227091-3dd870d97f1d?w=800"
            ),
            List.of("100% handcrafted genuine top-grain leather", "RFID blocking security shield protection", "8 card slots, 2 currency compartments, and ID window", "Compact slim bifold profile"),
            Map.of("Material", "Top-Grain Leather", "Dimensions", "11 x 9 x 2 cm", "RFID Safe", "Yes", "Color", "Hunter Tan"),
            "Engineered with RFID blocking technology and handcrafted in premium top-grain leather for timeless style."
        ));

        list.add(new SeedItem(
            "Allen Solly Men's Slim Fit Formal Office Trousers",
            "Allen Solly", "Fashion", 1799, 2799, 50, 4.5, 230,
            List.of(
                "https://images.unsplash.com/photo-1479064555552-3ef4979f8908?w=800",
                "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800"
            ),
            List.of("Wrinkle-resistant poly-viscose blend", "Flat front slim tailoring for modern profile", "Side slant pockets and welt back pockets", "Comfort stretch waistband"),
            Map.of("Material", "65% Polyester, 35% Viscose", "Fit", "Slim Fit", "Pattern", "Solid", "Occasion", "Formal / Business"),
            "Sharp tailoring meets everyday comfort with Allen Solly's smart flat-front formal trousers."
        ));

        list.add(new SeedItem(
            "Fabindia Handblock Printed Chanderi Silk Dupatta",
            "Fabindia", "Fashion", 1690, 2490, 35, 4.8, 120,
            List.of(
                "https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?w=800",
                "https://images.unsplash.com/photo-1583391733975-045353597b83?w=800"
            ),
            List.of("Authentic traditional handblock printing", "Chanderi silk cotton with subtle golden zari border", "Tassel trim edges on both hems", "Lightweight and royal sheen"),
            Map.of("Fabric", "Chanderi Silk Cotton", "Dimensions", "2.5m x 0.9m", "Craft", "Handblock Print", "Care", "Dry Clean Only"),
            "Add timeless Indian artisanal heritage to your ethnic attire with Fabindia's handblock Chanderi dupatta."
        ));

        list.add(new SeedItem(
            "Woodland Men's Rugged Leather Outdoor Hiking Boots",
            "Woodland", "Fashion", 4395, 5995, 40, 4.7, 490,
            List.of(
                "https://images.unsplash.com/photo-1520639888713-7851133b1ed0?w=800",
                "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800"
            ),
            List.of("Heavy duty nubuck leather construction", "Deep-lugged rubber sole for extreme traction", "Padded collar and tongue for ankle support", "Rust-proof brass lace eyelets"),
            Map.of("Upper", "Genuine Nubuck Leather", "Sole", "High-Grip Thermoplastic Rubber", "Type", "Hiking / Outdoor", "Color", "Khaki / Camel"),
            "Built for the rugged outdoors. Woodland boots deliver unmatched durability, grip, and ankle protection."
        ));


        // ==========================================
        // 3. HOME (15 PRODUCTS)
        // ==========================================
        list.add(new SeedItem(
            "Dyson V15 Detect Cordless Stick Vacuum Cleaner",
            "Dyson", "Home", 59900, 65900, 20, 4.9, 160,
            List.of(
                "https://images.unsplash.com/photo-1558317374-067fb5f30001?w=800",
                "https://images.unsplash.com/photo-1527515637462-cff94eecc1ac?w=800"
            ),
            List.of("Laser illumination reveals invisible dust on hard floors", "Piezo sensor automatically counts and measures dust particles", "240 AW powerful suction with 60 min runtime", "Fully-sealed filtration captures 99.99% of microscopic particles"),
            Map.of("Suction Power", "240 AW", "Runtime", "Up to 60 Minutes", "Weight", "3.0 kg", "Bin Volume", "0.77 Liters"),
            "Dyson's most powerful, intelligent cordless vacuum with laser illumination technology."
        ));

        list.add(new SeedItem(
            "Philips Hue Smart LED Ambient Light Starter Kit",
            "Philips", "Home", 8999, 11999, 35, 4.7, 240,
            List.of(
                "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800",
                "https://images.unsplash.com/photo-1540932239986-30128078f3c5?w=800"
            ),
            List.of("16 million colors and 50,000 shades of warm-to-cool white light", "Includes Hue Bridge hub and 3 Smart Bulbs", "Works with Alexa, Apple HomeKit, and Google Assistant", "Syncs lighting with music, movies, and games"),
            Map.of("Wattage", "9W (60W equivalent)", "Cap Type", "B22 / E27", "Connectivity", "Zigbee & Bluetooth", "Colors", "16 Million"),
            "Transform your home lighting experience with Philips Hue smart connected lighting."
        ));

        list.add(new SeedItem(
            "Instant Pot Duo 7-in-1 Electric Pressure Multi-Cooker (6L)",
            "Instant Pot", "Home", 7999, 12999, 45, 4.8, 520,
            List.of(
                "https://images.unsplash.com/photo-1588854337236-6889d631faa8?w=800",
                "https://images.unsplash.com/photo-1544025162-d76694265947?w=800"
            ),
            List.of("7 appliances in 1: Pressure Cooker, Slow Cooker, Rice Cooker, Steamer, Saute, Yogurt Maker & Warmer", "13 customizable Smart Programs with one-touch cooking", "Stainless steel inner pot with tri-ply bottom", "Over 10 safety mechanisms with safety lock lid"),
            Map.of("Capacity", "6 Liters", "Power", "1000 Watts", "Material", "Food-Grade Stainless Steel", "Programs", "13 One-Touch"),
            "America's most loved multi-cooker makes cooking healthy, delicious meals up to 70% faster."
        ));

        list.add(new SeedItem(
            "Nespresso Vertuo Pop Automatic Coffee & Espresso Machine",
            "Nespresso", "Home", 14999, 18999, 25, 4.7, 180,
            List.of(
                "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=800",
                "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800"
            ),
            List.of("Centrifusion extraction technology spins capsule at 4,000 RPM", "Brews 4 cup sizes: Espresso, Double Espresso, Gran Lungo, Mug", "One-touch automated brewing with barcode capsule reading", "Fast 30-second heat-up time with auto shut-off"),
            Map.of("Water Tank", "0.6 Liters", "Cup Sizes", "4 Types", "Heating Time", "30 Seconds", "Color", "Mango Yellow"),
            "Add a pop of color to your kitchen while brewing full-bodied barista quality coffee at home."
        ));

        list.add(new SeedItem(
            "SleepyCat Orthopedic Memory Foam King Size Mattress (78x72x8)",
            "SleepyCat", "Home", 17499, 24999, 15, 4.8, 380,
            List.of(
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=800",
                "https://images.unsplash.com/photo-1582582621959-48d27397dc69?w=800"
            ),
            List.of("High-density orthopedic contouring support foam", "Cooling Gel Memory Foam layer prevents heat buildup", "Bamboo fiber breathable removable machine-washable cover", "Zero partner disturbance motion isolation"),
            Map.of("Size", "King (78 x 72 inches)", "Thickness", "8 Inches", "Warranty", "10 Years", "Firmness", "Medium-Firm"),
            "Engineered with orthopedic spinal alignment support to provide restorative, cool, deep sleep."
        ));

        list.add(new SeedItem(
            "IKEA Strandmon High-Back Wingback Armchair (Nordvalla Dark Gray)",
            "IKEA", "Home", 18990, 22990, 12, 4.8, 145,
            List.of(
                "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=800",
                "https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?w=800"
            ),
            List.of("High backrest gives optimal neck and head support", "Durable woven Nordvalla cover with classic piping", "Solid beech wood legs with dark stained finish", "Classic 1950s Scandinavian vintage design"),
            Map.of("Dimensions", "82 x 96 x 101 cm", "Seat Height", "45 cm", "Frame", "Solid Wood & Plywood", "Max Load", "110 kg"),
            "You can really relax and unwind in comfort, because the high back on this armchair provides extra support for your neck."
        ));

        list.add(new SeedItem(
            "Prestige 5-Piece Granite Non-Stick Induction Cookware Set",
            "Prestige", "Home", 3999, 6495, 60, 4.6, 610,
            List.of(
                "https://images.unsplash.com/photo-1584990347449-399e53068e1a?w=800",
                "https://images.unsplash.com/photo-1590794056226-79ef3a8147e1?w=800"
            ),
            List.of("5-layer German technology non-stick granite coating", "Gas and Induction stove compatible heavy-gauge base", "Includes Kadai with glass lid, Fry Pan, Omni Tawa, and Spatula", "PFOA free healthy cooking with minimal oil"),
            Map.of("Pieces", "5 Items", "Base", "Induction & Gas", "Coating", "5-Layer Granite Spatter", "Dishwasher Safe", "Yes"),
            "Revolutionary granite spatter coating designed for heavy daily cooking with superior non-stick durability."
        ));

        list.add(new SeedItem(
            "Xiaomi Smart Air Purifier 4 with True HEPA & OLED Screen",
            "Xiaomi", "Home", 12999, 15999, 30, 4.7, 280,
            List.of(
                "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=800",
                "https://images.unsplash.com/photo-1584905066893-7d5c142ba4e1?w=800"
            ),
            List.of("High-efficiency True HEPA filter removes 99.97% particles down to 0.3 microns", "High CADR of 400 m³/h cleans room up to 516 sq ft in 10 mins", "Real-time PM2.5 display with laser particle sensor", "Smart App control with Alexa and Google Assistant support"),
            Map.of("CADR", "400 m³/h", "Coverage Area", "516 sq ft", "Filter Life", "Up to 12 Months", "Noise Level", "32.1 dB Night Mode"),
            "Clean, fresh air for your whole family with high-precision laser particulate detection and 360-degree filtration."
        ));

        list.add(new SeedItem(
            "Tupperware Modular Kitchen Airtight Container Set (Pack of 6)",
            "Tupperware", "Home", 2499, 3600, 75, 4.7, 430,
            List.of(
                "https://images.unsplash.com/photo-1590794056226-79ef3a8147e1?w=800",
                "https://images.unsplash.com/photo-1584990347449-399e53068e1a?w=800"
            ),
            List.of("100% airtight and moisture-proof silicone seal", "Stackable modular space-saving design", "BPA-free virgin food-grade plastic with clear view window", "Keeps pulses, grains, and dry snacks fresh for months"),
            Map.of("Set Count", "6 Containers (2x 2.2L, 2x 1.7L, 2x 1.1L)", "Material", "BPA Free Polypropylene", "Airtight", "Yes", "Dishwasher Safe", "Yes"),
            "Organize your kitchen pantry with Tupperware's legendary airtight, stackable storage system."
        ));

        list.add(new SeedItem(
            "Story@Home Velvet Blackout Room Darkening Eyelet Curtains (Set of 2)",
            "Story@Home", "Home", 1499, 2999, 50, 4.6, 310,
            List.of(
                "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=800",
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=800"
            ),
            List.of("Blocks 90% sunlight and harmful UV rays", "Triple-weave heavy velvet fabric provides thermal insulation", "8 rust-proof silver metal eyelets per panel", "Noise reducing and energy saving room decor"),
            Map.of("Dimensions", "7 Feet Door (4ft x 7ft each)", "Quantity", "2 Panels", "Material", "Triple Weave Polyester Velvet", "Color", "Slate Gray"),
            "Elevate your bedroom ambience with heavy velvet blackout curtains that block external glare and noise."
        ));

        list.add(new SeedItem(
            "Milton Thermosteel 1000ml Vacuum Insulated 24-Hr Flask",
            "Milton", "Home", 999, 1320, 110, 4.8, 890,
            List.of(
                "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800",
                "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=800"
            ),
            List.of("100% 304 grade rust-free stainless steel inside and out", "Double-walled vacuum insulation keeps liquids hot or cold for 24 hours", "Leak-proof twist and pour stopper with cup cap", "Includes protective fabric carry pouch with strap"),
            Map.of("Capacity", "1000 ml (1 Liter)", "Insulation", "24 Hours Hot / Cold", "Steel Grade", "SS 304", "Leak Proof", "Yes"),
            "The quintessential insulated steel flask that keeps your beverages steaming hot or ice cold all day."
        ));

        list.add(new SeedItem(
            "Urban Ladder Sheesham Wood Solid Coffee Table with Shelf",
            "Urban Ladder", "Home", 8499, 13999, 15, 4.7, 110,
            List.of(
                "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?w=800",
                "https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=800"
            ),
            List.of("100% kiln-dried solid Sheesham wood (Indian Rosewood)", "Spacious tabletop with convenient lower magazine shelf", "Rich teak walnut protective lacquer finish", "Sturdy tapered block legs with 100kg load capacity"),
            Map.of("Dimensions", "90 x 50 x 42 cm", "Wood Type", "Solid Sheesham Wood", "Finish", "Teak Walnut", "Assembly", "Pre-Assembled"),
            "Handcrafted in solid Sheesham wood, this minimalist coffee table brings warmth and functional elegance to your living room."
        ));

        list.add(new SeedItem(
            "Bajaj Majesty 1.7L Stainless Steel Cordless Electric Kettle",
            "Bajaj", "Home", 1199, 1999, 85, 4.6, 740,
            List.of(
                "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=800",
                "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=800"
            ),
            List.of("1500W powerful heating element boils water in under 3 minutes", "360-degree swivel cordless base with cord storage", "Automatic boil-dry protection and auto shut-off safety", "Ergonomic cool-touch handle with single-touch lid opening"),
            Map.of("Capacity", "1.7 Liters", "Power", "1500 Watts", "Body", "Brushed Stainless Steel", "Auto Shut-Off", "Yes"),
            "Boil water for tea, coffee, and instant noodles effortlessly with Bajaj's fast-heating stainless steel kettle."
        ));

        list.add(new SeedItem(
            "Artisan Stoneware 16-Piece Matte Ceramic Dinnerware Set",
            "ClayCraft", "Home", 3299, 5499, 30, 4.7, 195,
            List.of(
                "https://images.unsplash.com/photo-1614088685112-0a760b71a3c8?w=800",
                "https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=800"
            ),
            List.of("Service for 4: 4 Dinner Plates, 4 Salad Plates, 4 Soup Bowls, 4 Mugs", "Handcrafted premium chip-resistant ceramic stoneware", "Matte glaze finish with organic speckled texture", "Microwave, Oven, and Dishwasher safe"),
            Map.of("Pieces", "16 Pieces (Service for 4)", "Material", "Ceramic Stoneware", "Microwave Safe", "Yes", "Color", "Charcoal Gray"),
            "Set a modern, earthy dining table with artisan ceramic stoneware crafted for everyday dining and festive hosting."
        ));

        list.add(new SeedItem(
            "Bombay Dyeing 100% Cotton 300 TC King Size Bed Sheet Set",
            "Bombay Dyeing", "Home", 1899, 3199, 55, 4.8, 380,
            List.of(
                "https://images.unsplash.com/photo-1631679706909-1844bbd07221?w=800",
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=800"
            ),
            List.of("100% pure long-staple combed cotton with 300 Thread Count sateen weave", "Includes 1 King Size Bed Sheet (108 x 108 inch) and 2 Large Pillow Covers", "Super soft, breathable, and hypoallergenic fabric", "Colorfast reactive printing resists fading wash after wash"),
            Map.of("Thread Count", "300 TC Sateen", "Dimensions", "274 x 274 cm (108x108 in)", "Material", "100% Combed Cotton", "Pattern", "Botanical Geometric"),
            "Experience 5-star hotel luxury at home with Bombay Dyeing's silky smooth 300 TC pure cotton bed sheet."
        ));


        // ==========================================
        // 4. GROCERY (15 PRODUCTS)
        // ==========================================
        list.add(new SeedItem(
            "Daawat Ultima Extra Long Grain Aged Basmati Rice (5 kg)",
            "Daawat", "Grocery", 949, 1250, 80, 4.8, 620,
            List.of(
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800",
                "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=800"
            ),
            List.of("Aged for 2 years for rich aroma and extra fluffy non-sticky grains", "Grains elongate up to 24mm upon cooking", "Pristine pearly white grains with rich nutty fragrance", "Ideal for Royal Biryani and Pulao dishes"),
            Map.of("Weight", "5 Kilograms", "Diet Type", "Vegetarian", "Grain Length", "Extra Long 8.4mm", "Shelf Life", "24 Months"),
            "The finest Basmati rice from the foothills of the Himalayas, aged for 2 years to perfection for royal feasts."
        ));

        list.add(new SeedItem(
            "Borges Extra Virgin Cold Pressed Olive Oil (1 Liter Glass Bottle)",
            "Borges", "Grocery", 1299, 1850, 60, 4.8, 410,
            List.of(
                "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=800",
                "https://images.unsplash.com/photo-1541256942802-7b2996a848c1?w=800"
            ),
            List.of("Extracted from 100% Spanish olives on the first cold press", "Rich in heart-healthy MUFA and natural antioxidants", "Maximum acidity below 0.5% for superior taste", "Ideal for salads, dressings, dips, and Mediterranean cooking"),
            Map.of("Volume", "1000 ml", "Origin", "Spain", "Container", "UV Protected Glass Bottle", "Extraction", "First Cold Pressed"),
            "Crafted from the finest Spanish olives, Borges Extra Virgin Olive Oil preserves natural aroma and antioxidants."
        ));

        list.add(new SeedItem(
            "California Whole Raw Premium Crunchy Almonds (500 g)",
            "NuttyGrubs", "Grocery", 449, 699, 120, 4.7, 850,
            List.of(
                "https://images.unsplash.com/photo-1508061252427-0ac60f584497?w=800",
                "https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=800"
            ),
            List.of("100% natural, whole California Badam nuts", "Rich source of Vitamin E, Magnesium, and Dietary Fiber", "Zero cholesterol and zero trans fats", "Vacuum packed in resealable zip pouch for lasting crunch"),
            Map.of("Weight", "500 Grams", "Nut Type", "California Almonds", "Packaging", "Resealable Zip Pouch", "Allergen", "Contains Tree Nuts"),
            "Premium grade whole California almonds packed with natural proteins and antioxidants for daily wellness."
        ));

        list.add(new SeedItem(
            "Blue Tokai Roasted Whole Arabica Coffee Beans - Attikan Estate (250g)",
            "Blue Tokai", "Grocery", 470, 550, 90, 4.9, 390,
            List.of(
                "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=800",
                "https://images.unsplash.com/photo-1447933601403-0c6688de566e?w=800"
            ),
            List.of("100% Specialty Grade Indian Arabica single-origin coffee", "Medium-Dark Roast with tasting notes of Dark Chocolate and Fig", "Freshly roasted in small artisanal batches", "One-way degassing valve keeps whole beans fresh"),
            Map.of("Weight", "250 Grams", "Roast Level", "Medium-Dark", "Estate", "Attikan Estate, Biligiriranga Hills", "Flavor Notes", "Dark Chocolate, Fig"),
            "Freshly roasted single-estate specialty Arabica beans featuring deep dark chocolate and sweet fruit tasting notes."
        ));

        list.add(new SeedItem(
            "Lindt Excellence 85% Cocoa Extra Dark Chocolate Bar (100g)",
            "Lindt", "Grocery", 315, 375, 140, 4.8, 560,
            List.of(
                "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=800",
                "https://images.unsplash.com/photo-1548848221-0c2e497ed557?w=800"
            ),
            List.of("Masterfully crafted with 85% pure premium cocoa solids", "Deep, robust cocoa aroma with balanced roasted notes", "Silky smooth melt created by Lindt Swiss Master Chocolatiers", "Low sugar content, perfect for gourmet dark chocolate connoisseurs"),
            Map.of("Weight", "100 Grams", "Cocoa Content", "85%", "Origin", "Switzerland", "Vegetarian", "Yes"),
            "Experience the ultimate cocoa intensity crafted with finesse by Swiss Master Chocolatiers."
        ));

        list.add(new SeedItem(
            "Twinings Pure Green Tea Bags - 100% Natural Antioxidants (100 Bags)",
            "Twinings", "Grocery", 499, 699, 85, 4.7, 430,
            List.of(
                "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=800",
                "https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=800"
            ),
            List.of("100 individually foil-wrapped tea envelopes for maximum freshness", "Delicate steamed green tea leaves with gentle aroma", "Naturally rich in beneficial catechins and antioxidants", "Zero calories when consumed without milk and sugar"),
            Map.of("Count", "100 Tea Bags", "Caffeine", "Low Caffeine", "Origin", "Zhejiang, China", "Ingredients", "100% Pure Green Tea"),
            "A delicate, smooth green tea with clean refreshing taste and natural vitality in every cup."
        ));

        list.add(new SeedItem(
            "Dabur 100% Pure Organic Forest Honey (1 kg Glass Squeezer)",
            "Dabur", "Grocery", 499, 650, 95, 4.7, 780,
            List.of(
                "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=800",
                "https://images.unsplash.com/photo-1558642452-9d2a7deb7f62?w=800"
            ),
            List.of("100% pure raw unprocessed forest honey tested for NMR purity", "No added sugar or artificial syrups (0% adulteration guarantee)", "Natural source of minerals, enzymes, and instant energy", "Comes in easy-pour, mess-free glass squeezer"),
            Map.of("Weight", "1000 Grams (1 kg)", "Purity", "NMR Tested Pure", "Packaging", "Glass Jar", "Diet Type", "Vegetarian"),
            "Golden, pure, NMR-verified organic forest honey straight from nature's hives to your table."
        ));

        list.add(new SeedItem(
            "Kellogg's Crunchy Almond & Cranberry Granola with Oats (500 g)",
            "Kellogg's", "Grocery", 349, 425, 75, 4.6, 320,
            List.of(
                "https://images.unsplash.com/photo-1517093707575-d144e50587a8?w=800",
                "https://images.unsplash.com/photo-1584776296944-ab6fb57b0bdd?w=800"
            ),
            List.of("Baked multigrain clusters of golden rolled oats, wheat, and barley", "Loaded with real sliced California almonds and dried cranberries", "High in dietary fiber and essential plant nutrients", "Delicious with chilled milk, yogurt bowls, or straight from the bag"),
            Map.of("Weight", "500 Grams", "Main Ingredients", "Rolled Oats, Almonds, Cranberries", "Diet Type", "Vegetarian", "Container", "Resealable Pouch"),
            "Kickstart your mornings with the crunchy delight of baked oat clusters, sliced almonds, and juicy cranberries."
        ));

        list.add(new SeedItem(
            "Nutella Hazelnut Cocoa Creamy Breakfast Spread (750 g)",
            "Nutella", "Grocery", 599, 720, 100, 4.9, 940,
            List.of(
                "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=800",
                "https://images.unsplash.com/photo-1548848221-0c2e497ed557?w=800"
            ),
            List.of("Iconic recipe of roasted Italian hazelnuts and creamy skimmed milk cocoa", "Free from artificial colors and preservatives", "Smooth spreadable texture on pancakes, toasts, waffles, and fruits", "750g family value glass jar"),
            Map.of("Weight", "750 Grams", "Ingredients", "Hazelnuts (13%), Cocoa, Skimmed Milk", "Origin", "Ferrero Italy", "Container", "Glass Jar"),
            "The world's favorite hazelnut cocoa spread transforms breakfast into an irresistible treat."
        ));

        list.add(new SeedItem(
            "Tata Sampann Unpolished High-Protein Toor Dal (1 kg)",
            "Tata Sampann", "Grocery", 185, 230, 110, 4.7, 510,
            List.of(
                "https://images.unsplash.com/photo-1515543237350-b3eea1ec8082?w=800",
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800"
            ),
            List.of("100% unpolished lentils without water, oil, or leather polish", "Retains natural dietary fiber, plant protein, and wholesome grain aroma", "Strict 5-step quality screening for clean uniform grains", "Rich in folic acid, iron, and potassium"),
            Map.of("Weight", "1000 Grams", "Polish", "Unpolished", "Protein Content", "22g per 100g", "Brand", "Tata Sampann"),
            "Pure, wholesome, unpolished toor dal that delivers authentic homestyle taste and wholesome nutrition."
        ));

        list.add(new SeedItem(
            "Saffola Gold Pro Healthy Heart Blended Cooking Oil (5 L Jar)",
            "Saffola", "Grocery", 899, 1199, 65, 4.7, 680,
            List.of(
                "https://images.unsplash.com/photo-1620706857370-e1b9770e8bb1?w=800",
                "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=800"
            ),
            List.of("Dual Seed Technology: Blend of 80% Refined Rice Bran & 20% Refined Sunflower Oil", "Oryzanol technology helps manage cholesterol levels", "LOSORB technology reduces up to 33% oil absorption in fried food", "Enriched with Vitamin A and Vitamin D"),
            Map.of("Volume", "5 Liters", "Container", "5L Can with Handle", "Blend", "Rice Bran & Sunflower", "Fortified", "Vitamins A & D"),
            "Smart cooking oil formulated with natural antioxidants and Oryzanol to care for your family's heart health."
        ));

        list.add(new SeedItem(
            "Hershey's Exotic Dark Chocolates - California Cranberry & Blueberry (100g)",
            "Hershey's", "Grocery", 140, 175, 120, 4.6, 380,
            List.of(
                "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=800",
                "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=800"
            ),
            List.of("Exotic whole dried cranberries and blueberries coated in rich dark chocolate", "Unique contrast of velvety dark cocoa and sweet tart berries", "Smooth premium cocoa melt with intense fruit core", "Packaged in elegant gift box"),
            Map.of("Weight", "100 Grams", "Flavors", "California Cranberry & Blueberry", "Cocoa Type", "Dark Chocolate", "Vegetarian", "Yes"),
            "An exquisite combination of luxurious dark chocolate draped over succulent whole exotic berries."
        ));

        list.add(new SeedItem(
            "Aashirvaad Superior MP Sharbati Whole Wheat Atta (5 kg)",
            "Aashirvaad", "Grocery", 279, 340, 90, 4.8, 710,
            List.of(
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800",
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800"
            ),
            List.of("Made from 100% authentic MP Sharbati wheat grains from Sehore", "Absorbs more water to make rotis that stay soft and fluffy for hours", "Traditional chakki process locks in natural dietary fiber and golden color", "Zero maida, 100% whole grain purity"),
            Map.of("Weight", "5 Kilograms", "Wheat Type", "100% MP Sharbati", "Chakki Ground", "Yes", "Preservatives", "Zero"),
            "Crafted with the golden grains of Madhya Pradesh, Aashirvaad Sharbati atta produces the softest, sweetest rotis."
        ));

        list.add(new SeedItem(
            "Epigamia Natural Greek Yogurt - High Protein & No Preservatives (400g)",
            "Epigamia", "Grocery", 120, 150, 60, 4.6, 230,
            List.of(
                "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=800",
                "https://images.unsplash.com/photo-1517093707575-d144e50587a8?w=800"
            ),
            List.of("Thick, creamy strained Greek yogurt with double the protein of regular dahi", "Loaded with active gut-friendly probiotic live cultures", "Zero preservatives, zero artificial sweeteners, zero gelatin", "Low fat, high calcium nutrient-dense snack"),
            Map.of("Weight", "400 Grams", "Protein", "6g per 100g", "Probiotics", "Live Cultures", "Storage", "Keep Refrigerated (4°C)"),
            "Velvety, thick Greek yogurt strained traditionally to deliver double protein and probiotic digestive wellness."
        ));

        list.add(new SeedItem(
            "Ferrero Rocher Premium Hazelnut Pralines Gift Box (24 Pieces)",
            "Ferrero Rocher", "Grocery", 899, 1099, 70, 4.9, 980,
            List.of(
                "https://images.unsplash.com/photo-1548848221-0c2e497ed557?w=800",
                "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=800"
            ),
            List.of("Whole crunchy roasted hazelnut in the center", "Creamy hazelnut filling wrapped in crispy wafer shell", "Draped in fine milk chocolate and chopped roasted hazelnuts", "Individually wrapped in golden luxury foil"),
            Map.of("Count", "24 Pralines (300g)", "Origin", "Italy", "Gift Box", "Transparent Golden Box", "Vegetarian", "Yes"),
            "A timeless symbol of luxury and celebratory gifting. A whole roasted hazelnut encased in creamy chocolate and crispy wafer."
        ));


        // ==========================================
        // 5. BEAUTY (15 PRODUCTS)
        // ==========================================
        list.add(new SeedItem(
            "Minimalist 10% Vitamin C Brightening Face Serum (30ml)",
            "Minimalist", "Beauty", 664, 699, 85, 4.8, 540,
            List.of(
                "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800",
                "https://images.unsplash.com/photo-1608248597359-2e11893d9220?w=800"
            ),
            List.of("10% Ethyl Ascorbic Acid (stable Vitamin C derivative)", "Centella Asiatica water soothes skin and prevents irritation", "Reduces dark spots, pigmentation, and dullness in 4 weeks", "Fragrance-free, non-comedogenic, clean formula"),
            Map.of("Volume", "30 ml", "Active Ingredient", "10% Vitamin C + 1% Acetyl Glucosamine", "Skin Type", "All Skin Types", "pH", "3.8 - 4.8"),
            "A glow-boosting daily serum formulated with stable Vitamin C to reveal radiant, even-toned skin."
        ));

        list.add(new SeedItem(
            "Neutrogena Hydro Boost Hyaluronic Acid Water Gel Moisturizer (50g)",
            "Neutrogena", "Beauty", 849, 1150, 70, 4.8, 620,
            List.of(
                "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=800",
                "https://images.unsplash.com/photo-1556228722-d0b5d0339d67?w=800"
            ),
            List.of("72-hour continuous hydration with purified Hyaluronic Acid", "Ultra-lightweight oil-free water gel texture absorbs instantly", "Strengthens skin natural moisture barrier", "Dermatologist tested and non-comedogenic"),
            Map.of("Weight", "50 Grams", "Texture", "Oil-Free Water Gel", "Main Ingredient", "Hyaluronic Acid & Trehalose", "Skin Type", "Normal to Oily"),
            "Instantly quenches dry skin and keeps it looking smooth, supple, and hydrated day after day."
        ));

        list.add(new SeedItem(
            "MAC Retro Matte Iconic Lipstick - Ruby Woo (3g)",
            "M.A.C", "Beauty", 1950, 2300, 45, 4.9, 480,
            List.of(
                "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=800",
                "https://images.unsplash.com/photo-1599305090598-fe179d501227?w=800"
            ),
            List.of("The world's most famous vivid blue-red matte shade", "Ultra-matte finish with intense high-pigment payoff", "Long-wearing 12-hour non-feathering wear", "Flattering on all skin undertones"),
            Map.of("Shade", "Ruby Woo (Vivid Blue-Red)", "Finish", "Retro Matte", "Weight", "3.0 g", "Wear Time", "12 Hours"),
            "The iconic shade that started it all. Ruby Woo delivers intense color payoff in a completely matte finish."
        ));

        list.add(new SeedItem(
            "The Ordinary Niacinamide 10% + Zinc 1% Oil Control Serum (30ml)",
            "The Ordinary", "Beauty", 550, 650, 95, 4.7, 720,
            List.of(
                "https://images.unsplash.com/photo-1608248597359-2e11893d9220?w=800",
                "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800"
            ),
            List.of("High-strength vitamin and mineral blemish formula", "10% Niacinamide (Vitamin B3) minimizes enlarged pores", "1% Zinc PCA balances visible sebum activity", "Cruelty-free, vegan, alcohol-free, and silicone-free"),
            Map.of("Volume", "30 ml", "Actives", "10% Niacinamide, 1% Zinc PCA", "Target", "Blemishes & Oiliness", "pH", "5.5 - 6.5"),
            "Regulate excess sebum production and reduce the appearance of enlarged pores with high-potency Niacinamide."
        ));

        list.add(new SeedItem(
            "La Roche-Posay Anthelios SPF 60 Ultra Light Invisible Fluid Sunscreen (50ml)",
            "La Roche-Posay", "Beauty", 1850, 2200, 35, 4.9, 310,
            List.of(
                "https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=800",
                "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=800"
            ),
            List.of("Broad-spectrum UVA/UVB protection with Mexoryl 400 filter", "Ultra-fluid invisible finish leaves zero white cast", "80-minute water and sweat resistant", "Suitable for sensitive and allergy-prone skin"),
            Map.of("Volume", "50 ml", "SPF", "SPF 60 Broad Spectrum", "Finish", "Invisible Matte", "Water Resistant", "80 Minutes"),
            "Dermatologist-recommended ultra-light sunscreen offering the highest UVA protection with an undetectable finish."
        ));

        list.add(new SeedItem(
            "Olaplex No. 3 Hair Perfector Bond Repair Treatment (100ml)",
            "Olaplex", "Beauty", 2950, 3450, 40, 4.8, 410,
            List.of(
                "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=800",
                "https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=800"
            ),
            List.of("Patented Bis-Aminopropyl Diglycol Dimaleate bond building technology", "Repairs damaged and compromised hair from chemical color and heat", "Strengthens hair structure, reduces breakage, and restores shine", "Pre-shampoo intensive treatment for all hair types"),
            Map.of("Volume", "100 ml", "Hair Type", "All Damaged / Colored Hair", "Sulfate-Free", "Yes", "Paraben-Free", "Yes"),
            "The globally acclaimed bond-builder that repairs split ends, reduces breakage, and noticeably transforms damaged hair."
        ));

        list.add(new SeedItem(
            "Dior Sauvage Eau De Parfum for Men (100ml Luxury Spray)",
            "Dior", "Beauty", 11500, 13500, 25, 4.9, 580,
            List.of(
                "https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=800",
                "https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?w=800"
            ),
            List.of("Sensual fragrance notes of Calabrian Bergamot and spicy Sichuan Pepper", "Deep amber-woody base of Papua New Guinean Vanilla absolute", "Incredible 12+ hour sillage and projection", "Refillable luxury magnetic-cap glass bottle"),
            Map.of("Volume", "100 ml", "Concentration", "Eau De Parfum (EDP)", "Fragrance Family", "Fresh Amber Woody", "Gender", "Men"),
            "A powerful, noble fragrance inspired by the magic of the desert twilight and raw wide-open spaces."
        ));

        list.add(new SeedItem(
            "Forest Essentials Ayurvedic Kashmiri Saffron Face Cleanser (200ml)",
            "Forest Essentials", "Beauty", 1475, 1750, 50, 4.8, 290,
            List.of(
                "https://images.unsplash.com/photo-1556228722-d0b5d0339d67?w=800",
                "https://images.unsplash.com/photo-1616683693504-3ea7e9ad6fec?w=800"
            ),
            List.of("Infused with pure organic Kashmiri Saffron and sweet Neem leaves", "Gently removes impurities and environmental toxins without stripping oils", "Steam-distilled floral essential waters promote glow", "100% Ayurvedic certified formulation"),
            Map.of("Volume", "200 ml", "Key Ingredients", "Kashmiri Saffron, Neem, Rosewater", "Sulfate-Free", "Yes", "Type", "Facial Wash"),
            "A delicate Ayurvedic facial cleanser with royal Kashmiri saffron that leaves skin deeply purified and radiant."
        ));

        list.add(new SeedItem(
            "Maybelline New York Lash Sensational Waterproof Mascara - Very Black",
            "Maybelline", "Beauty", 479, 599, 110, 4.7, 860,
            List.of(
                "https://images.unsplash.com/photo-1631214524020-7e18db9a8f92?w=800",
                "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=800"
            ),
            List.of("Exclusive 10-layer fanning brush unfolds every single lash", "Waterproof and smudge-proof formula lasts up to 24 hours", "Enriched with conditioning rose hip oil and mineral pigments", "Ophthalmologist tested, suitable for contact lens wearers"),
            Map.of("Shade", "Very Black (Waterproof)", "Volume", "9.5 ml", "Wear", "24 Hours Waterproof", "Formula", "Ophthalmologist Tested"),
            "Reveal full-fan volume from root to tip with the iconic curved Lash Sensational silicone wand."
        ));

        list.add(new SeedItem(
            "Kama Ayurveda Pure Steam-Distilled Rose Water Face Mist (200ml)",
            "Kama Ayurveda", "Beauty", 1450, 1695, 60, 4.8, 370,
            List.of(
                "https://images.unsplash.com/photo-1616683693504-3ea7e9ad6fec?w=800",
                "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800"
            ),
            List.of("Steam-distilled from handpicked Kannauj Damascena roses", "Natural alcohol-free facial toner that tightens pores and balances pH", "Restores hydration and revives tired skin instantly", "Subtle, soothing natural floral fragrance"),
            Map.of("Volume", "200 ml", "Ingredient", "100% Pure Rosa Damascena Flower Water", "Alcohol-Free", "Yes", "Packaging", "Fine Mist Spray Bottle"),
            "Pure Kannauj rose water created through traditional hydro-distillation to tone and rejuvenate your skin."
        ));

        list.add(new SeedItem(
            "CeraVe Hydrating Facial Cleanser for Normal to Dry Skin (236ml)",
            "CeraVe", "Beauty", 1199, 1499, 75, 4.8, 590,
            List.of(
                "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=800",
                "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=800"
            ),
            List.of("Formulated with 3 essential ceramides (1, 3, 6-II) and Hyaluronic Acid", "MVE Delivery Technology provides 24-hour continuous moisture release", "Non-foaming creamy lotion cleanses without disrupting skin barrier", "National Eczema Association accepted"),
            Map.of("Volume", "236 ml", "Key Actives", "Ceramides 1, 3, 6-II, Hyaluronic Acid", "Skin Type", "Dry to Normal", "Non-Comedogenic", "Yes"),
            "Developed with dermatologists, this gentle hydrating cleanser washes away dirt while replenishing the skin barrier."
        ));

        list.add(new SeedItem(
            "Laneige Lip Sleeping Mask - Berry Intense Overnight Hydration (20g)",
            "Laneige", "Beauty", 1250, 1450, 90, 4.9, 680,
            List.of(
                "https://images.unsplash.com/photo-1599305090598-fe179d501227?w=800",
                "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=800"
            ),
            List.of("Berry Fruit Complex with Vitamin C and rich antioxidants", "Moisture Wrap technology locks in active moisture overnight", "Melts away dead skin cells for baby-soft lips by morning", "Delicious sweet berry aroma with applicator spatula included"),
            Map.of("Weight", "20 Grams", "Flavor", "Berry", "Benefit", "Overnight Lip Repair & Plumping", "Includes", "Silicone Applicator"),
            "Wake up to irresistibly soft, hydrated, supple lips with Laneige's bestselling overnight lip mask."
        ));

        list.add(new SeedItem(
            "Estée Lauder Advanced Night Repair Synchronized Multi-Recovery Complex (50ml)",
            "Estée Lauder", "Beauty", 7900, 9200, 20, 4.8, 260,
            List.of(
                "https://images.unsplash.com/photo-1617897903246-719242758050?w=800",
                "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800"
            ),
            List.of("Chronolux Power Signal Technology boosts natural collagen production", "Provides 72-hour deep moisture and 8-hour antioxidant defense", "Visibly reduces lines, wrinkles, uneven tone, and pores", "Oil-free, fragrance-free formula in apothecary glass dropper"),
            Map.of("Volume", "50 ml", "Skin Benefit", "Anti-Aging, Firming, Radiance", "Technology", "Chronolux Power Signal", "Dropper", "Glass Pipette"),
            "The #1 anti-aging serum in the world. Experience rapid visible repair and youthful radiance with ANR."
        ));

        list.add(new SeedItem(
            "L'Oreal Paris Extraordinary Nourishing Hair Oil Serum (100ml)",
            "L'Oreal Paris", "Beauty", 549, 699, 100, 4.6, 520,
            List.of(
                "https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=800",
                "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=800"
            ),
            List.of("Infused with 6 precious floral oil extracts (Lotus, Chamomile, Tiare, Rose)", "Multi-use elixir: pre-wash, before blow-dry, or finishing serum", "Transforms dry frizzy hair into silky smooth, glossy tresses", "Non-greasy lightweight texture with heat protection"),
            Map.of("Volume", "100 ml", "Ingredients", "6 Rare Flower Oils", "Hair Type", "Dry, Frizzy, Dull Hair", "Heat Protectant", "Yes"),
            "A drop of liquid gold. Transform dry, unruly hair into lustrous, salon-glossy locks with zero greasiness."
        ));

        list.add(new SeedItem(
            "Philips Satinelle Essential Compact Corded Epilator for Women",
            "Philips", "Beauty", 2299, 2995, 45, 4.5, 340,
            List.of(
                "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=800",
                "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=800"
            ),
            List.of("Gentle tweezing discs remove hairs as short as 0.5mm from root", "2 speed settings to grab thinner and thicker hairs effortlessly", "Washable epilation head for hygienic easy cleaning", "Ergonomic rounded profile fits perfectly in your hand"),
            Map.of("Power", "Corded 15V", "Speeds", "2 Speed Settings", "Washable Head", "Yes", "Included", "Cleaning Brush"),
            "Enjoy weeks of hair-free, satin-smooth skin with Philips Satinelle compact epilation system."
        ));


        // ==========================================
        // 6. SPORTS (15 PRODUCTS)
        // ==========================================
        list.add(new SeedItem(
            "Yonex Astrox 99 Pro High-Tension Badminton Racket (4U / G5)",
            "Yonex", "Sports", 14990, 19990, 30, 4.9, 230,
            List.of(
                "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=800",
                "https://images.unsplash.com/photo-1613918108466-292b78a8ef95?w=800"
            ),
            List.of("Rotational Generator System with Namd graphite", "Head-heavy balance for steep explosive smashes", "Energy Boost Cap Plus maximizes shaft flex", "Chosen by World Champions Kento Momota & Viktor Axelsen"),
            Map.of("Weight", "4U (83g)", "Grip Size", "G5", "Flex", "Extra Stiff", "String Tension", "Up to 28 lbs"),
            "Dominate the badminton court with steep, unreturnable power smashes powered by the Astrox 99 Pro."
        ));

        list.add(new SeedItem(
            "Boldfit NBR Extra Thick 10mm Anti-Slip Exercise Yoga Mat with Carry Strap",
            "Boldfit", "Sports", 899, 1699, 90, 4.7, 780,
            List.of(
                "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=800",
                "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800"
            ),
            List.of("10mm high density cushioning cushions spine, hips, knees, and elbows", "Double-sided non-slip ribbed texture prevents sliding on marble/wood", "Moisture resistant eco-friendly NBR material is easy to wash", "Includes free elastic carry strap"),
            Map.of("Thickness", "10 mm", "Dimensions", "183 x 61 cm (6 x 2 ft)", "Material", "Eco-Friendly NBR", "Carry Strap", "Included"),
            "Super comfortable 10mm high-density cushioning designed for yoga, pilates, stretching, and floor workouts."
        ));

        list.add(new SeedItem(
            "Nivia Storm All-Weather Machine Stitched Football (Size 5)",
            "Nivia", "Sports", 499, 750, 110, 4.6, 650,
            List.of(
                "https://images.unsplash.com/photo-1614632537423-1e6c2e7e0aab?w=800",
                "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800"
            ),
            List.of("32-panel machine-stitched PU synthetic leather outer casing", "Reinforced rubber bladder provides superior air retention and true flight", "High abrasion resistance for grass, turf, and hard outdoor ground", "Official Match Ball standard Size 5"),
            Map.of("Size", "Size 5 (Official)", "Panels", "32 Panels", "Material", "PU Synthetic Leather", "Ground Type", "All Surfaces"),
            "India's favorite training football. Engineered for rugged performance and predictable aerodynamic flight."
        ));

        list.add(new SeedItem(
            "Bowflex SelectTech 552 Adjustable Dumbbells Pair (2.5 to 24 kg)",
            "Bowflex", "Sports", 28990, 39990, 15, 4.9, 140,
            List.of(
                "https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=800",
                "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800"
            ),
            List.of("Replaces 15 sets of weights with a single dial turn system", "Adjusts from 2.5 kg up to 24 kg in smooth increments", "Durable molding around metal plates creates a quiet, smooth lift", "Includes dedicated storage docking trays"),
            Map.of("Weight Range", "2.5 to 24 kg per dumbbell", "Increments", "15 Weight Settings", "Pieces", "Pair (2 Dumbbells)", "Warranty", "2 Years"),
            "Transform your home gym. Seamlessly adjust weights with the turn of a dial from 2.5 to 24 kg."
        ));

        list.add(new SeedItem(
            "Decathlon Kipsta 35L Water Resistant Sport Duffel Gym Bag",
            "Decathlon", "Sports", 1299, 1999, 85, 4.7, 430,
            List.of(
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800",
                "https://images.unsplash.com/photo-1547949003-9792a18a2601?w=800"
            ),
            List.of("Spacious 35-liter main compartment with separate ventilated shoe pocket", "Reinforced water-repellent base and abrasion-resistant fabric", "Adjustable padded shoulder strap and dual carry handles", "Folds into its own side pocket for easy storage"),
            Map.of("Capacity", "35 Liters", "Shoe Compartment", "Yes (Ventilated)", "Material", "100% Coated Polyester", "Weight", "450g"),
            "Durable, practical, and water-resistant. Features a dedicated shoe compartment to keep your gear organized."
        ));

        list.add(new SeedItem(
            "Garmin Forerunner 265 AMOLED GPS Running & Triathlon Smartwatch",
            "Garmin", "Sports", 44990, 50490, 20, 4.9, 110,
            List.of(
                "https://images.unsplash.com/photo-1510017803434-a899398421b3?w=800",
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800"
            ),
            List.of("Brilliant 1.3-inch colorful AMOLED touchscreen display", "Training Readiness score based on sleep quality, recovery, and HRV", "Multi-band GNSS GPS tracking with SatIQ technology", "Up to 13 days of battery life in smartwatch mode"),
            Map.of("Display", "1.3 inch AMOLED", "Battery Life", "13 Days (Smartwatch), 20 Hrs (GPS)", "Water Rating", "5 ATM", "Sensors", "Wrist Heart Rate, Pulse Ox, Multi-GNSS"),
            "Plan your race strategy with personalized daily suggested workouts, training readiness metrics, and AMOLED clarity."
        ));

        list.add(new SeedItem(
            "Cosco Championship Light Tennis Balls Can (Pack of 12)",
            "Cosco", "Sports", 849, 1150, 95, 4.6, 520,
            List.of(
                "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=800",
                "https://images.unsplash.com/photo-1617083934555-563d67964b0f?w=800"
            ),
            List.of("ITF Approved tournament quality pressurized rubber core", "High-visibility extra-duty woven felt cover for consistent bounce", "Durable seam construction for clay, grass, and hard courts", "Pack of 12 balls in pressurized canisters"),
            Map.of("Quantity", "12 Balls (4 Cans of 3)", "Approval", "ITF Approved", "Court Surface", "All Court", "Color", "Optic Yellow"),
            "ITF approved championship grade tennis balls engineered for optimal bounce, spin, and durability across all courts."
        ));

        list.add(new SeedItem(
            "Strauss Adjustable Heavy-Duty Hand Grip Strengthener (10 to 60 kg)",
            "Strauss", "Sports", 299, 599, 140, 4.7, 980,
            List.of(
                "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800",
                "https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=800"
            ),
            List.of("Adjustable resistance dial from 10 kg to 60 kg for progressive overload", "Heavy-duty stainless steel spring and non-slip rubberized handles", "Builds forearm, wrist, and finger strength for athletics and climbing", "Compact and portable for travel and office workouts"),
            Map.of("Resistance Range", "10 - 60 kg", "Material", "Alloy Steel & TPR Rubber", "Counter", "Mechanical Dial", "Color", "Black/Orange"),
            "Increase grip strength and forearm vascularity with this smoothly adjustable, heavy-duty resistance hand gripper."
        ));

        list.add(new SeedItem(
            "Under Armour Men's HeatGear Compression Long-Sleeve Rashguard",
            "Under Armour", "Sports", 2499, 3999, 45, 4.8, 310,
            List.of(
                "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800",
                "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=800"
            ),
            List.of("Ultra-tight second-skin compression fit boosts blood circulation", "HeatGear fabric wicks sweat and dries exceptionally fast", "4-way stretch material moves better in every direction", "Strategic mesh underarm and back panels for ventilation"),
            Map.of("Material", "84% Polyester, 16% Elastane", "Fit", "Ultra-Tight Compression", "UPF Rating", "UPF 30+", "Sleeve", "Full Sleeve"),
            "The original performance baselayer that keeps athletes cool, dry, and locked-in during intense gym training."
        ));

        list.add(new SeedItem(
            "Speedo Fastskin Elite Anti-Fog UV Shield Swimming Goggles",
            "Speedo", "Sports", 1999, 2999, 50, 4.7, 240,
            List.of(
                "https://images.unsplash.com/photo-1530549387789-4c1017266635?w=800",
                "https://images.unsplash.com/photo-1519315901367-f34ff9154487?w=800"
            ),
            List.of("Hydrodynamic low-profile lens reduces water drag", "IQfit 3D goggle seal ensures a leak-free, secure fit without marks", "Anti-fog mirror coated lenses provide 180-degree wide peripheral vision", "Includes interchangeable nose bridges for custom fit"),
            Map.of("Lens", "Mirror Coated Polycarbonate", "UV Protection", "100% UVA/UVB", "Seal", "IQfit 3D Silicone", "Approval", "World Aquatics (FINA)"),
            "World Aquatics approved racing goggles engineered for maximum hydrodynamics, wide vision, and zero leakage."
        ));

        list.add(new SeedItem(
            "SG Savage Edition Grade 1 English Willow Cricket Bat (Short Handle)",
            "SG", "Sports", 8999, 12999, 25, 4.9, 180,
            List.of(
                "https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=800",
                "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=800"
            ),
            List.of("Handcrafted from premium unbleached Grade 1 English Willow", "Massive 38-40mm edges with full spine profile for monster strokeplay", "Round 12-piece cane handle with Chevtec grip for control", "Pre-knocked and fitted with toe guard"),
            Map.of("Willow", "Grade 1 English Willow", "Weight", "1160 - 1200g", "Handle", "12-Piece Saravak Cane", "Sweet Spot", "Mid to Low"),
            "Handcrafted for explosive boundary-hitting power with thick edges and light pickup favored by international batsmen."
        ));

        list.add(new SeedItem(
            "Hydro Flask 32 oz Wide Mouth Vacuum Insulated Water Bottle (Pacific Blue)",
            "Hydro Flask", "Sports", 3499, 4499, 65, 4.8, 420,
            List.of(
                "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800",
                "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=800"
            ),
            List.of("TempShield double-wall vacuum insulation keeps ice cold for 24 hours", "Pro-grade 18/8 stainless steel ensures pure taste without flavor transfer", "Color Last powder coat is slip-free, sweat-free, and dishwasher safe", "Wide mouth opening accommodates ice cubes and sport filters"),
            Map.of("Capacity", "32 oz (946 ml)", "Insulation", "24 Hr Cold / 12 Hr Hot", "Material", "18/8 Pro-Grade Steel", "BPA Free", "Yes"),
            "Stay hydrated on trails and in the gym with Hydro Flask's iconic TempShield insulated wide-mouth bottle."
        ));

        list.add(new SeedItem(
            "TRX GO Suspension Training Bodyweight Gym System",
            "TRX", "Sports", 9999, 14999, 20, 4.8, 160,
            List.of(
                "https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=800",
                "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800"
            ),
            List.of("Complete full-body workout system utilizing your own bodyweight", "Sets up in less than 60 seconds on any door, beam, or outdoor tree", "Lightweight packable travel kit weighs under 1 lb", "Tested for users up to 350 lbs (158 kg)"),
            Map.of("Weight Capacity", "350 lbs (158 kg)", "System Weight", "450g", "Anchors Included", "Door & Suspension Anchor", "Material", "Commercial Webbing"),
            "The original bodyweight suspension trainer. Build lean muscle, burn fat, and strengthen your core anywhere."
        ));

        list.add(new SeedItem(
            "Reebok Speed Rope Pro High-Speed Bearing Skipping Rope",
            "Reebok", "Sports", 699, 1299, 90, 4.6, 510,
            List.of(
                "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=800",
                "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800"
            ),
            List.of("360-degree ball bearing rotation mechanism for lightning fast double-unders", "3-meter steel wire cable with protective polymer coating", "Adjustable length screw mechanism fits athletes of all heights", "Non-slip textured slim aluminum handles"),
            Map.of("Cable Length", "300 cm (Adjustable)", "Cable Material", "Coated Steel Wire", "Bearing", "360° High Speed", "Weight", "180g"),
            "Engineered for high-cadence double-unders and cardio conditioning with smooth frictionless ball bearings."
        ));

        list.add(new SeedItem(
            "Wilson US Open Official Pro Tennis Racket (Grip 3 / Unstrung)",
            "Wilson", "Sports", 11990, 16990, 25, 4.8, 190,
            List.of(
                "https://images.unsplash.com/photo-1617083934555-563d67964b0f?w=800",
                "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=800"
            ),
            List.of("High Performance Carbon Fiber frame for explosive baseline power", "100 sq inch headsize provides a forgiving, generous sweetspot", "Parallel Drilling technology provides consistent string response", "Official licensed racket of the US Open Championship"),
            Map.of("Head Size", "100 sq in (645 sq cm)", "Unstrung Weight", "300 g", "String Pattern", "16x19", "Balance", "32 cm / 7 pts HL"),
            "Unleash aggressive groundstrokes with precision control and explosive spin powered by Wilson carbon fiber technology."
        ));


        // ==========================================
        // 7. TOYS (15 PRODUCTS)
        // ==========================================
        list.add(new SeedItem(
            "LEGO Star Wars Millennium Falcon 75257 Starship Building Kit (1353 Pieces)",
            "LEGO", "Toys", 13999, 17999, 20, 4.9, 320,
            List.of(
                "https://images.unsplash.com/photo-1585366119957-e9730b6d0f60?w=800",
                "https://images.unsplash.com/photo-1560859251-d563a49c5e4a?w=800"
            ),
            List.of("1,353 pieces authentic Star Wars starship replica", "Includes 7 LEGO Minifigures: Finn, Chewbacca, Lando, C-3PO, Boolio, D-O & R2-D2", "Opening top panels reveal detailed interior with hyperdrive, cargo area & galley", "Rotating top and bottom laser turrets with 2 spring-loaded shooters"),
            Map.of("Pieces", "1353 Pieces", "Age Group", "9+ Years", "Dimensions", "44 x 32 x 14 cm", "Theme", "Star Wars: The Rise of Skywalker"),
            "Inspire kids and adult collectors with the ultimate LEGO Millennium Falcon starship featuring intricate interior details."
        ));

        list.add(new SeedItem(
            "DJI Mini 4 Pro Remote Control Drone with 4K HDR Camera & Obstacle Sensing",
            "DJI", "Toys", 74990, 89990, 15, 4.9, 190,
            List.of(
                "https://images.unsplash.com/photo-1527977966376-1c8408f9f108?w=800",
                "https://images.unsplash.com/photo-1508614589041-895b88991e3e?w=800"
            ),
            List.of("Under 249g lightweight folding design (No FAA registration required in many regions)", "Omnidirectional obstacle sensing with APAS 5.0", "4K/60fps HDR True Vertical Shooting for social video", "Up to 34 minutes flight time with 20km FHD video transmission"),
            Map.of("Takeoff Weight", "249 g", "Flight Time", "34 Minutes", "Video", "4K 60fps HDR", "Sensor", "1/1.3-inch CMOS"),
            "Capture breathtaking aerial footage with omnidirectional obstacle avoidance and 4K HDR vertical shooting."
        ));

        list.add(new SeedItem(
            "Hot Wheels 20-Car Collector Diecast Vehicle Gift Pack (1:64 Scale)",
            "Hot Wheels", "Toys", 1999, 2999, 65, 4.8, 640,
            List.of(
                "https://images.unsplash.com/photo-1594787318286-3d835c1d207f?w=800",
                "https://images.unsplash.com/photo-1581235720704-06d3acfcb36f?w=800"
            ),
            List.of("Includes 20 distinct 1:64 scale diecast metal vehicles", "Realistic details, authentic decos, and rolling wheels", "Track compatible with all Hot Wheels playsets", "Packaged in a display window gift box"),
            Map.of("Scale", "1:64 Scale", "Material", "Die-Cast Metal & ABS", "Car Count", "20 Vehicles", "Age", "3+ Years"),
            "Jumpstart your car collection with 20 high-speed Hot Wheels die-cast vehicles featuring classic and futuristic models."
        ));

        list.add(new SeedItem(
            "Barbie Dreamhouse 3-Story Interactive Playset with Pool & Slide",
            "Barbie", "Toys", 16999, 21999, 12, 4.8, 175,
            List.of(
                "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=800",
                "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?w=800"
            ),
            List.of("3 stories, 8 rooms with 360-degree play and working elevator", "Spiral slide leads into real fillable swimming pool", "Lights and sound accessories with 5 custom song modes", "Includes over 70 furniture pieces and accessories"),
            Map.of("Height", "110 cm (43 inches)", "Rooms", "8 Rooms across 3 Levels", "Batteries", "3 AAA Required", "Age", "3 - 10 Years"),
            "Measuring 43 inches tall, the Barbie Dreamhouse features 8 furnished rooms, working elevator, slide, and light-up sounds."
        ));

        list.add(new SeedItem(
            "Monopoly Ultimate Banking Edition Electronic Board Game",
            "Hasbro Gaming", "Toys", 1899, 2799, 50, 4.7, 430,
            List.of(
                "https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=800",
                "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800"
            ),
            List.of("Cashless gameplay with all-in-one Ultimate Banking digital unit", "Tap-to-play bank cards make buying and rent transactions instant", "Event cards and changing property values create dynamic games", "Faster paced modern edition of the classic board game"),
            Map.of("Players", "2 to 4 Players", "Age", "8+ Years", "Electronic Unit", "Included (Batteries Req)", "Game Time", "45 - 60 Mins"),
            "Experience modern fast-paced Monopoly with tap-and-pay digital banking cards and fluctuating real-estate values."
        ));

        list.add(new SeedItem(
            "Fisher-Price Deluxe Kick & Play Piano Baby Musical Gym",
            "Fisher-Price", "Toys", 2499, 3999, 55, 4.8, 510,
            List.of(
                "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?w=800",
                "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=800"
            ),
            List.of("4 ways to play as baby grows: Lay & play, Tummy time, Sit & play, Take-along", "Smart Stages learning technology with 65+ songs, sounds, and phrases", "Large 5-key light-up piano and repositionable toy arch", "Machine-washable thick plush play mat"),
            Map.of("Age Group", "0 - 36 Months", "Modes", "4 Growth Stages", "Toy Arch", "5 Repositionable Toys", "Mat", "Machine Washable"),
            "Engage baby's senses with vibrant lights, musical piano keys, and hanging tactile toys designed for sensory development."
        ));

        list.add(new SeedItem(
            "Nerf Elite 2.0 Commander RD-6 Dart Blaster with 12 Official Darts",
            "Nerf", "Toys", 1199, 1799, 70, 4.7, 490,
            List.of(
                "https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=800",
                "https://images.unsplash.com/photo-1585366119957-e9730b6d0f60?w=800"
            ),
            List.of("6-dart rotating cylinder drum for rapid fire", "Includes 12 official Nerf Elite foam darts tested for performance", "Tactical rails and barrel/stock attachment points for custom upgrades", "Fires darts up to 90 feet (27 meters)"),
            Map.of("Firing Range", "Up to 90 ft (27m)", "Dart Capacity", "6-Dart Drum", "Included Darts", "12 Elite Foam Darts", "Age", "8+ Years"),
            "Gear up for battle with customizable tactical rails and rapid 6-dart rotating cylinder action."
        ));

        list.add(new SeedItem(
            "Rubik's Connected Smart Bluetooth 3x3 Electronic Speed Cube",
            "Rubik's", "Toys", 3499, 4999, 35, 4.8, 160,
            List.of(
                "https://images.unsplash.com/photo-1591991731833-b4807cf7ef94?w=800",
                "https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=800"
            ),
            List.of("Equipped with Bluetooth sensors that track every move in real time", "Companion mobile app teaches how to solve the cube step-by-step", "Mini games, progress analytics, and global online battle mode", "Magnetic speed cube mechanism with USB rechargeable battery"),
            Map.of("Connectivity", "Bluetooth 5.0 (iOS & Android)", "Mechanism", "Magnetic Speed Cube", "Battery", "USB Rechargeable (60 Hrs)", "Age", "8+ Years"),
            "Connect the classic Rubik's cube to your phone! Learn to solve in hours and race cubers around the globe."
        ));

        list.add(new SeedItem(
            "Play-Doh Classic Non-Toxic Modeling Compound Mega Pack (24 Colors)",
            "Play-Doh", "Toys", 999, 1499, 90, 4.8, 590,
            List.of(
                "https://images.unsplash.com/photo-1560859251-d563a49c5e4a?w=800",
                "https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=800"
            ),
            List.of("24 vibrant colors in individual airtight 3-ounce cans", "100% non-toxic wheat-based modeling compound", "Soft, squishy texture encourages imaginative fine-motor creativity", "Great for arts and crafts, school projects, and preschool play"),
            Map.of("Can Count", "24 Cans (3 oz each)", "Total Weight", "2.04 kg Compound", "Non-Toxic", "Yes (ASTM D-4236)", "Age", "2+ Years"),
            "Open a rainbow of creative possibilities with 24 individual cans of classic non-toxic Play-Doh modeling compound."
        ));

        list.add(new SeedItem(
            "Sphero BOLT App-Enabled Programmable STEM Robot Ball",
            "Sphero", "Toys", 12999, 16999, 18, 4.9, 120,
            List.of(
                "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=800",
                "https://images.unsplash.com/photo-1527977966376-1c8408f9f108?w=800"
            ),
            List.of("8x8 LED matrix displays animations, sensor data, and characters", "Programmable infrared sensors, compass, light sensor & gyroscope", "Program via Draw, Scratch blocks, or JavaScript in Sphero Edu app", "Durable waterproof transparent polycarbonate shell"),
            Map.of("Shell", "Waterproof Polycarbonate", "Battery", "Inductive Charging (2+ Hours)", "Sensors", "IMU, Light, IR, Compass", "Ages", "8+ Years"),
            "Empower young coders with an app-enabled robotic ball equipped with an 8x8 LED matrix and advanced sensors."
        ));

        list.add(new SeedItem(
            "Melissa & Doug Solid Wood Building Blocks Set in Wooden Crate (100 Pieces)",
            "Melissa & Doug", "Toys", 1899, 2799, 50, 4.8, 380,
            List.of(
                "https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=800",
                "https://images.unsplash.com/photo-1560859251-d563a49c5e4a?w=800"
            ),
            List.of("100 solid wood blocks in 4 colors and 9 distinct architectural shapes", "Smooth rounded edges with non-toxic child-safe paints", "Develops spatial awareness, hand-eye coordination, and sorting skills", "Includes sturdy wooden storage tray"),
            Map.of("Pieces", "100 Blocks", "Material", "Solid Natural Wood", "Shapes", "Cubes, Cylinders, Arches, Prisms", "Age", "2 - 8 Years"),
            "Timeless classic building blocks crafted in smooth solid wood to inspire hours of open-ended building fun."
        ));

        list.add(new SeedItem(
            "Funko Pop! Marvel Avengers: Infinity War Spider-Man Vinyl Figure",
            "Funko", "Toys", 1099, 1499, 85, 4.8, 620,
            List.of(
                "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800",
                "https://images.unsplash.com/photo-1594787318286-3d835c1d207f?w=800"
            ),
            List.of("Stylized 3.75-inch tall Iron Spider vinyl bobble-head figure", "Detailed gold mechanical waldoes spider legs on back", "Collector-friendly window display packaging box", "Official Marvel licensed merchandise"),
            Map.of("Height", "3.75 Inches (9.5 cm)", "Material", "Vinyl", "Figure Type", "Bobble-Head", "Series", "Marvel Avengers #300"),
            "The iconic web-slinger in his Iron Spider armor, stylized in Funko's signature oversized head vinyl format."
        ));

        list.add(new SeedItem(
            "VTech KidiZoom Duo 5.0 MP Digital Camera for Kids (Shockproof)",
            "VTech", "Toys", 4299, 5999, 30, 4.6, 210,
            List.of(
                "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800",
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800"
            ),
            List.of("Dual 5.0 MP front and rear selfie cameras with 4x digital zoom", "Durable rubberized shockproof casing withstands drops and bumps", "2.4-inch color LCD screen with fun photo effects, frames, and voice changers", "Built-in memory with microSD card expansion slot"),
            Map.of("Resolution", "5.0 Megapixels", "Screen", "2.4 inch Color TFT", "Features", "Dual Cameras, Voice Recorder, 5 Games", "Age", "3 - 9 Years"),
            "A real digital camera built tough for little photographers with dual lenses, creative filters, and games."
        ));

        list.add(new SeedItem(
            "Ravensburger 1000-Piece Dolomites Mountain Landscape Jigsaw Puzzle",
            "Ravensburger", "Toys", 1399, 1999, 45, 4.9, 290,
            List.of(
                "https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=800",
                "https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?w=800"
            ),
            List.of("1000 precision cut pieces featuring Softclick Technology", "Stunning high-resolution photograph of the Italian Dolomites mountain range", "Anti-glare linen-structure cardboard surface", "Every piece is unique and interlocking seamlessly"),
            Map.of("Piece Count", "1000 Pieces", "Finished Size", "70 x 50 cm", "Material", "Premium Extra-Thick Cardboard", "Age", "12+ Years"),
            "Immerse yourself in mindful relaxation with Ravensburger's bestselling European mountain landscape puzzle."
        ));

        list.add(new SeedItem(
            "Disney Frozen 2 Elsa Interactive Musical Adventure Fashion Doll",
            "Disney", "Toys", 1799, 2699, 60, 4.7, 390,
            List.of(
                "https://images.unsplash.com/photo-1558877385-81a1c7e67d72?w=800",
                "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=800"
            ),
            List.of("Sings hit song 'Into the Unknown' when her bodice button is pressed", "Bodice lights up in dynamic sparkling icy blue patterns", "Removable shimmer organza cape and movie-inspired travel dress", "Poseable fashion doll with long blonde braided hair"),
            Map.of("Height", "11.5 Inches (29 cm)", "Batteries", "3 LR44 Included", "Song", "Into the Unknown", "Age", "3+ Years"),
            "Sing along with Elsa as her dress illuminates in icy wonder with this magical musical Frozen 2 fashion doll."
        ));


        // ==========================================
        // 8. BOOKS (15 PRODUCTS)
        // ==========================================
        list.add(new SeedItem(
            "Atomic Habits: An Easy & Proven Way to Build Good Habits by James Clear",
            "James Clear", "Books", 499, 799, 150, 4.9, 980,
            List.of(
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800",
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800"
            ),
            List.of("#1 New York Times Bestseller with over 15 million copies sold", "The Four Laws of Behavior Change framework", "Practical strategies to break bad habits and master tiny behaviors", "Hardcover collector edition with satin ribbon bookmark"),
            Map.of("Format", "Hardcover", "Pages", "320 Pages", "Publisher", "Random House Business", "Language", "English"),
            "A revolutionary system to get 1% better every day. Atomic Habits provides practical strategies that teach you how to form good habits."
        ));

        list.add(new SeedItem(
            "The Psychology of Money: Timeless Lessons on Wealth by Morgan Housel",
            "Morgan Housel", "Books", 349, 499, 120, 4.9, 870,
            List.of(
                "https://images.unsplash.com/photo-1592496431122-2349e0fbc666?w=800",
                "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=800"
            ),
            List.of("19 short stories exploring the strange ways people think about money", "Focuses on behavioral psychology rather than mathematical formulas", "Global bestseller translated into over 50 languages", "Essential reading for personal finance and investing"),
            Map.of("Format", "Paperback", "Pages", "256 Pages", "Publisher", "Harriman House", "ISBN", "978-9390166268"),
            "Doing well with money isn't necessarily about what you know. It's about how you behave. Essential lessons on wealth, greed, and happiness."
        ));

        list.add(new SeedItem(
            "Clean Code: A Handbook of Agile Software Craftsmanship by Robert C. Martin",
            "Robert C. Martin", "Books", 699, 999, 85, 4.8, 640,
            List.of(
                "https://images.unsplash.com/photo-1532012164546-f432f2e37b73?w=800",
                "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800"
            ),
            List.of("The definitive guide on writing readable, maintainable software", "Includes case studies on refactoring code smell into pristine code", "Best practices for meaningful naming, functions, classes, and unit testing", "Crucial reference for every software developer and architect"),
            Map.of("Format", "Paperback", "Pages", "464 Pages", "Publisher", "Pearson Education", "Topic", "Software Engineering"),
            "Even bad code can function. But if code isn't clean, it can bring a development organization to its knees. Master clean coding practices."
        ));

        list.add(new SeedItem(
            "Sapiens: A Brief History of Humankind by Yuval Noah Harari",
            "Yuval Noah Harari", "Books", 449, 699, 95, 4.9, 790,
            List.of(
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800"
            ),
            List.of("Over 25 million copies sold globally", "Explores the cognitive, agricultural, and scientific revolutions", "How an insignificant ape became the ruler of planet Earth", "Endorsed by Barack Obama, Bill Gates, and Mark Zuckerberg"),
            Map.of("Format", "Paperback", "Pages", "498 Pages", "Publisher", "Vintage", "Genre", "Anthropology & World History"),
            "One hundred thousand years ago, at least six different species of humans inhabited Earth. Yet today there is only one: Homo sapiens."
        ));

        list.add(new SeedItem(
            "Rich Dad Poor Dad: What the Rich Teach Their Kids by Robert T. Kiyosaki",
            "Robert T. Kiyosaki", "Books", 399, 599, 130, 4.8, 890,
            List.of(
                "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=800",
                "https://images.unsplash.com/photo-1592496431122-2349e0fbc666?w=800"
            ),
            List.of("#1 Personal Finance book of all time", "Explodes the myth that you need to earn a high income to become rich", "Defines the fundamental difference between assets and liabilities", "Teaches parents what to teach their kids about financial literacy"),
            Map.of("Format", "Paperback", "Pages", "336 Pages", "Publisher", "Plata Publishing", "Topic", "Financial Literacy"),
            "The timeless milestone in personal finance that challenges conventional wisdom on money, assets, and passive cashflow."
        ));

        list.add(new SeedItem(
            "Deep Work: Rules for Focused Success in a Distracted World by Cal Newport",
            "Cal Newport", "Books", 389, 550, 75, 4.8, 480,
            List.of(
                "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800",
                "https://images.unsplash.com/photo-1532012164546-f432f2e37b73?w=800"
            ),
            List.of("Wall Street Journal Bestseller on hyper-focused productivity", "4 rigorous rules for transforming your mind and working habits", "How to eliminate digital distraction and master hard things quickly", "Essential guidebook for knowledge workers and entrepreneurs"),
            Map.of("Format", "Paperback", "Pages", "304 Pages", "Publisher", "Grand Central Publishing", "Topic", "Productivity"),
            "Deep work is the ability to focus without distraction on a cognitively demanding task. A superpower in our increasingly competitive economy."
        ));

        list.add(new SeedItem(
            "Think and Grow Rich: The Landmark Bestseller by Napoleon Hill",
            "Napoleon Hill", "Books", 249, 399, 110, 4.7, 720,
            List.of(
                "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=800",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800"
            ),
            List.of("Over 100 million copies sold worldwide", "Contains the 13 proven principles of personal achievement", "Based on 25 years of research interviewing Andrew Carnegie, Thomas Edison & Henry Ford", "The definitive philosophy of wealth creation and success"),
            Map.of("Format", "Paperback", "Pages", "320 Pages", "Publisher", "Fingerprint! Publishing", "Genre", "Self-Help / Success"),
            "Napoleon Hill's timeless classic that has motivated millions to overcome obstacles and transform thoughts into tangible riches."
        ));

        list.add(new SeedItem(
            "Ikigai: The Japanese Secret to a Long and Happy Life by Hector Garcia",
            "Hector Garcia", "Books", 320, 550, 140, 4.8, 830,
            List.of(
                "https://images.unsplash.com/photo-1524578271613-d550eacf6090?w=800",
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800"
            ),
            List.of("International sensation based on interviews with centenarians of Okinawa", "How to discover your intersection of passion, mission, vocation, and profession", "Practical insights on diet, daily purpose, flow states, and mindful longevity", "Elegant hardcover gift edition with foiled lettering"),
            Map.of("Format", "Hardcover", "Pages", "208 Pages", "Publisher", "Penguin Life", "Language", "English"),
            "Discover your ikigai - the reason you get up in the morning. Bring meaning, joy, and longevity to your everyday existence."
        ));

        list.add(new SeedItem(
            "Dune by Frank Herbert - Deluxe Illustrated Hardcover Collector Edition",
            "Frank Herbert", "Books", 999, 1499, 40, 4.9, 410,
            List.of(
                "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=800",
                "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=800"
            ),
            List.of("Winner of the prestigious Hugo and Nebula Awards", "Set on the desert planet Arrakis, home of the spice melange", "Stunning deluxe edition with embossed gold foil cover, stained edges & maps", "The greatest and bestselling science fiction epic of all time"),
            Map.of("Format", "Deluxe Hardcover", "Pages", "688 Pages", "Publisher", "Ace Books", "Genre", "Epic Science Fiction"),
            "Frank Herbert's masterwork - the epic tale of Paul Atreides on the desert world of Arrakis. The triumph of science fiction literature."
        ));

        list.add(new SeedItem(
            "The Alchemist by Paulo Coelho - 25th Anniversary Deluxe Edition",
            "Paulo Coelho", "Books", 349, 499, 120, 4.8, 760,
            List.of(
                "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=800",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800"
            ),
            List.of("Over 65 million copies sold in more than 80 languages", "The magical journey of Santiago, an Andalusian shepherd boy following his dreams", "An inspiring fable about listening to our hearts and recognizing opportunity", "Includes a special foreword by Paulo Coelho"),
            Map.of("Format", "Paperback", "Pages", "208 Pages", "Publisher", "HarperOne", "Genre", "Philosophical Fiction"),
            "When you want something, all the universe conspires in helping you to achieve it. The transformative tale of Santiago's journey."
        ));

        list.add(new SeedItem(
            "Zero to One: Notes on Startups, or How to Build the Future by Peter Thiel",
            "Peter Thiel", "Books", 399, 599, 80, 4.8, 510,
            List.of(
                "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=800",
                "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800"
            ),
            List.of("#1 New York Times Bestseller on innovation and monopoly creation", "How to create things that have never existed before (0 to 1)", "Contrarian thinking and lessons from PayPal and early-stage Silicon Valley", "Essential masterclass for founders, investors, and innovators"),
            Map.of("Format", "Hardcover", "Pages", "224 Pages", "Publisher", "Crown Business", "Topic", "Entrepreneurship / Business"),
            "If you want to build a better future, you must believe in secrets. The great secret of our time is that there are still uncharted frontiers."
        ));

        list.add(new SeedItem(
            "Can't Hurt Me: Master Your Mind and Defy the Odds by David Goggins",
            "David Goggins", "Books", 499, 799, 90, 4.9, 810,
            List.of(
                "https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=800",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800"
            ),
            List.of("The gripping autobiography of Navy SEAL and ultra-endurance athlete David Goggins", "Introduces the 40% Rule to push past self-imposed mental limits", "Unfiltered raw account of overcoming childhood trauma, obesity, and adversity", "Over 5 million copies sold globally"),
            Map.of("Format", "Paperback", "Pages", "364 Pages", "Publisher", "Lioncrest Publishing", "Genre", "Memoir / Mental Toughness"),
            "For David Goggins, childhood was a nightmare. Through self-discipline, mental toughness, and hard work, he transformed into an American icon."
        ));

        list.add(new SeedItem(
            "Designing Data-Intensive Applications by Martin Kleppmann",
            "Martin Kleppmann", "Books", 1199, 1699, 65, 4.9, 490,
            List.of(
                "https://images.unsplash.com/photo-1532012164546-f432f2e37b73?w=800",
                "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800"
            ),
            List.of("The authoritative guide to data systems architecture, storage engines, and scalability", "In-depth breakdown of transactions, consensus, replication, and partitioning", "Covers Relational, NoSQL, Stream Processing, and Distributed Systems", "The gold standard book for Senior and Principal Software Engineers"),
            Map.of("Format", "Paperback", "Pages", "616 Pages", "Publisher", "O'Reilly Media", "Topic", "Distributed Systems / Backend"),
            "The big ideas behind reliable, scalable, and maintainable systems. Essential reading for system architects and backend engineers."
        ));

        list.add(new SeedItem(
            "The Pragmatic Programmer: Your Journey to Mastery (20th Anniversary Edition)",
            "David Thomas & Andrew Hunt", "Books", 899, 1299, 50, 4.8, 380,
            List.of(
                "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=800",
                "https://images.unsplash.com/photo-1532012164546-f432f2e37b73?w=800"
            ),
            List.of("Fully updated classic for modern software engineering craftsmanship", "Topics range from personal responsibility and career development to architectural techniques", "Over 100 practical tips and analogies that stick with you for life", "Foreword by Ward Cunningham"),
            Map.of("Format", "Paperback", "Pages", "352 Pages", "Publisher", "Addison-Wesley", "Topic", "Software Craftsmanship"),
            "One of the most significant books on software engineering. It cuts through the increasing specialization of modern software development."
        ));

        list.add(new SeedItem(
            "To Kill a Mockingbird by Harper Lee (Pulitzer Prize Winner)",
            "Harper Lee", "Books", 299, 450, 110, 4.9, 920,
            List.of(
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800",
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800"
            ),
            List.of("Pulitzer Prize winning masterpiece of American literature", "Explores justice, courage, and human empathy in the American South", "Over 40 million copies sold in over 40 languages", "Voted America's best-loved novel by PBS Great American Read"),
            Map.of("Format", "Paperback", "Pages", "384 Pages", "Publisher", "Harper Perennial", "Genre", "Classic Literature"),
            "The unforgettable novel of a childhood in a sleepy Southern town and the crisis of conscience that rocked it. A masterpiece of compassion."
        ));

        return list;
    }
}
