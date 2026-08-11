package com.example.spark_mart.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
    private final AtomicLong productIds = new AtomicLong(100);
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final TagRepository tagRepository;

    public CatalogService(CategoryRepository categoryRepository, ProductRepository productRepository,
            TagRepository tagRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.tagRepository = tagRepository;
    }

    @PostConstruct
    void initializeCatalog() {
        categoryRepository.saveAll(defaultCategories());
        Product latest = productRepository.findTopByOrderByIdDesc();
        if (latest != null) {
            productIds.set(Math.max(productIds.get(), latest.getId()));
        }
        if (productRepository.count() == 0) {
            seedDemoCatalog();
        }
        if (tagRepository.count() == 0) {
            tagRepository.saveAll(List.of(
                    new Tag("Bestseller"),
                    new Tag("New Arrival"),
                    new Tag("Eco-Friendly"),
                    new Tag("Limited Stock")));
        }
    }

    private List<Category> defaultCategories() {
        return List.of(
                new Category("grocery", "Grocery", "Daily essentials, pantry staples, and home supplies."),
                new Category("cosmetics-skincare", "Cosmetics & Girls Skincare",
                        "Care, beauty, girls skincare, and personal grooming."),
                new Category("womens-clothes", "Girls Clothes",
                        "Everyday wear, modest styles, and seasonal girls fashion picks."),
                new Category("electronics-devices", "Electronics / Devices",
                        "Useful devices, accessories, electronics, and gadgets."));
    }

    public List<Category> categories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findCategory(String slug) {
        return categoryRepository.findById(slug);
    }

    public Category saveCategory(String slug, String name, String description) {
        return categoryRepository.save(new Category(slug, name, description));
    }

    public void deleteCategory(String slug) {
        if (productRepository.findByCategorySlug(slug).isEmpty()) {
            categoryRepository.deleteById(slug);
        }
    }

    public List<Product> allProducts() {
        return productRepository.findAll();
    }

    public List<Product> featuredProducts() {
        return productRepository.findAll().stream()
                .sorted(Comparator.comparing(Product::getRating).reversed())
                .limit(6)
                .toList();
    }

    public List<Product> newArrivals() {
        return productRepository.findAll().stream()
                .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                .limit(4)
                .toList();
    }

    public Optional<Product> findProduct(long id) {
        return productRepository.findById(id);
    }

    // Loads a Product and converts it to a ProductForm (including its Tags)
    // entirely inside one transaction, so the lazy-loaded tags collection is
    // fetched while the Hibernate session is still open — this is what fixes
    // the LazyInitializationException seen when the admin opens the edit page.
    @Transactional(readOnly = true)
    public Optional<ProductForm> findProductForm(long id) {
        return productRepository.findById(id).map(ProductForm::from);
    }

    public List<Product> filter(String categorySlug, String query, BigDecimal minPrice, BigDecimal maxPrice,
            boolean inStockOnly, double minRating, String sort) {
        String normalizedQuery = normalize(query);
        Comparator<Product> comparator = comparator(sort);

        return productRepository.findAll().stream()
                .filter(product -> categorySlug == null || product.getCategorySlug().equals(categorySlug))
                .filter(product -> normalizedQuery.isBlank() || searchable(product).contains(normalizedQuery))
                .filter(product -> minPrice == null || product.getPrice().compareTo(minPrice) >= 0)
                .filter(product -> maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0)
                .filter(product -> !inStockOnly || product.isInStock())
                .filter(product -> product.getRating() >= minRating)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public Product aiSuggestionFor(String query) {
        String safeQuery = query == null || query.isBlank() ? "Requested Item" : titleCase(query);
        String categorySlug = detectCategorySlug(safeQuery);
        Category category = findCategory(categorySlug).orElseGet(() -> categories().get(0));
        Product product = new Product(productIds.incrementAndGet(), safeQuery + " Studio Edition",
                Product.slugify(safeQuery), category.slug(), category.name(), suggestedPrice(safeQuery),
                suggestedStock(safeQuery),
                "AI generated product for \"" + safeQuery
                        + "\" with a realistic category, practical stock level, fair demo price, and a complete catalog description.",
                suggestedImage(category.slug()), suggestedRating(safeQuery), true, LocalDateTime.now());
        return productRepository.save(product);
    }

    public Product save(ProductForm form) {
        Category category = findCategory(form.getCategorySlug()).orElse(categories().get(0));
        Product product = form.toProduct(productIds.incrementAndGet(), category.name());
        applyTags(product, form.getTagIds());
        return productRepository.save(product);
    }

    @Transactional
    public void update(long id, ProductForm form) {
        productRepository.findById(id).ifPresent(product -> {
            Category category = findCategory(form.getCategorySlug()).orElse(categories().get(0));
            product.updateFrom(form, category.name());
            applyTags(product, form.getTagIds());
            productRepository.save(product);
        });
    }

    private void applyTags(Product product, List<Long> tagIds) {
        Set<Tag> selected = tagIds == null || tagIds.isEmpty()
                ? new HashSet<>()
                : new HashSet<>(tagRepository.findAllById(tagIds));
        product.getTags().clear();
        product.getTags().addAll(selected);
    }

    public List<Tag> allTags() {
        return tagRepository.findAll();
    }

    public void adjustStock(long id, int quantityDelta) {
        productRepository.findById(id).ifPresent(product -> {
            product.adjustStock(quantityDelta);
            productRepository.save(product);
        });
    }

    public void delete(long id) {
        productRepository.deleteById(id);
    }

    public long lowStockCount() {
        return productRepository.findAll().stream().filter(product -> !product.isAiSuggested() && product.getStock() <= 5)
                .count();
    }

    public long onDemandCount() {
        return productRepository.findAll().stream().filter(Product::isAiSuggested).count();
    }

    private void seedDemoCatalog() {
        seedCategory("grocery", new String[][] {
                { "Premium Basmati Rice 5kg", "1550", "24",
                        "Long grain basmati rice with a naturally fragrant aroma, ideal for biryani, family meals, and special occasion cooking.",
                        "4.8" },
                { "Organic Lentil Pack", "280", "42",
                        "Protein-rich mixed lentils selected for quick cooking, everyday dal, soups, and balanced home meals.",
                        "4.6" },
                { "Aromatic Chinigura Rice 2kg", "620", "31",
                        "Fine aromatic rice with a soft texture and rich smell, perfect for pulao, khichuri, and festive dishes.",
                        "4.7" },
                { "Cold Pressed Mustard Oil 1L", "390", "28",
                        "Pure mustard oil with a bold traditional flavor for frying, bhorta, pickles, and Bangladeshi home cooking.",
                        "4.5" },
                { "Premium Soybean Oil 5L", "860", "19",
                        "Reliable cooking oil for daily frying and meal prep, packed for family kitchens and regular grocery use.",
                        "4.4" },
                { "Deshi Masoor Dal 1kg", "190", "54",
                        "Clean red lentils that cook quickly into smooth dal, offering a simple protein source for everyday lunch and dinner.",
                        "4.7" },
                { "Moong Dal Premium 1kg", "240", "33",
                        "Golden moong dal with a light nutty taste, suitable for khichuri, soups, and easy digestive meals.",
                        "4.6" },
                { "Chickpea Boot Dal 1kg", "260", "29",
                        "Hearty chickpea dal with firm texture and rich taste, useful for curries, snacks, and traditional recipes.",
                        "4.5" },
                { "Whole Wheat Atta 2kg", "210", "40",
                        "Freshly packed whole wheat flour for soft roti, paratha, and everyday breakfast or dinner preparation.",
                        "4.4" },
                { "Fine Flour Maida 2kg", "180", "36",
                        "Smooth refined flour for baking, snacks, luchi, porota, and homemade treats that need a soft finish.",
                        "4.3" },
                { "Brown Sugar 1kg", "170", "25",
                        "Light brown sugar with a gentle caramel note for tea, desserts, baking, and balanced sweet recipes.",
                        "4.4" },
                { "Iodized Salt 1kg", "45", "80",
                        "Everyday iodized salt with consistent grain size, packed for cooking, seasoning, and regular household use.",
                        "4.8" },
                { "Mixed Spice Box 12pcs", "720", "18",
                        "A curated spice box with essential powdered spices for curry, roast, grill, snacks, and daily cooking.",
                        "4.6" },
                { "Turmeric Powder 500g", "180", "34",
                        "Bright turmeric powder with strong color and earthy taste, useful for curries, marinades, and traditional recipes.",
                        "4.5" },
                { "Red Chili Powder 500g", "220", "27",
                        "Fresh red chili powder with balanced heat and color, suitable for curries, snacks, and spicy home dishes.",
                        "4.4" },
                { "Cumin Powder 250g", "240", "22",
                        "Ground cumin with warm aroma for meat, vegetables, lentils, and spice blends used in daily cooking.",
                        "4.6" },
                { "Premium Tea Leaves 400g", "360", "30",
                        "Strong black tea leaves with a full-bodied flavor, ideal for milk tea, morning cups, and evening refreshment.",
                        "4.7" },
                { "Instant Coffee Jar 200g", "520", "16",
                        "Smooth instant coffee that dissolves quickly, giving a rich cup at home, office, or while traveling.",
                        "4.3" },
                { "Honey Glass Jar 500g", "490", "21",
                        "Natural honey in a glass jar for tea, breakfast, desserts, and simple wellness routines.",
                        "4.5" },
                { "Peanut Butter Creamy 340g", "430", "17",
                        "Creamy peanut butter with roasted flavor, good for sandwiches, smoothies, breakfast bowls, and quick snacks.",
                        "4.4" },
                { "Mixed Nuts Pack 500g", "820", "13",
                        "A balanced mix of almonds, cashews, raisins, and peanuts for snacking, gifting, or adding to desserts.",
                        "4.6" },
                { "Oats Breakfast Pack 1kg", "390", "26",
                        "Whole grain oats for porridge, overnight oats, baking, and quick fiber-rich breakfast meals.",
                        "4.5" },
                { "Pasta Family Pack 1kg", "310", "32",
                        "Firm pasta that holds sauce well, suitable for quick dinners, lunch boxes, and family-size meals.",
                        "4.3" },
                { "Tomato Sauce 500g", "160", "38",
                        "Tangy tomato sauce for snacks, noodles, sandwiches, fries, and simple homemade fast-food recipes.",
                        "4.4" },
                { "Dishwash Liquid Lemon 1L", "290", "23",
                        "Lemon-scented dishwash liquid that cuts grease effectively while staying practical for everyday kitchen cleaning.",
                        "4.5" } });

        seedCategory("cosmetics-skincare", new String[][] {
                { "Hydrating Face Serum", "890", "18",
                        "Lightweight hydrating serum with a clean finish, designed for daily use before moisturizer and sunscreen.",
                        "4.7" },
                { "Matte Lip Color Set", "650", "12",
                        "Three wearable matte lip shades with comfortable texture, long wear, and a smooth everyday finish.",
                        "4.5" },
                { "Vitamin C Brightening Serum", "990", "20",
                        "Brightening serum formulated for dull-looking skin, helping improve glow and even-looking tone with regular use.",
                        "4.6" },
                { "Niacinamide Pore Care Serum", "850", "19",
                        "Daily serum that supports smoother-looking skin, balanced oil control, and a refined pore appearance.",
                        "4.5" },
                { "Aloe Vera Gel 300ml", "320", "35",
                        "Cooling aloe vera gel for face, body, and after-sun care, leaving skin refreshed without heaviness.",
                        "4.7" },
                { "Gentle Foaming Face Wash", "420", "27",
                        "Mild foaming cleanser that removes daily dirt and oil while keeping skin feeling soft and comfortable.",
                        "4.4" },
                { "Deep Clean Clay Mask", "540", "15",
                        "Mineral clay mask that helps absorb excess oil and refresh tired skin after a long day.",
                        "4.3" },
                { "Rose Water Toner 250ml", "360", "29",
                        "Refreshing rose water toner for quick hydration, light cleansing, and preparing skin before serum.",
                        "4.4" },
                { "Daily Moisture Cream", "580", "24",
                        "Soft daily moisturizer with a non-greasy feel, suitable for morning and night skincare routines.",
                        "4.6" },
                { "SPF 50 Sunscreen Lotion", "780", "22",
                        "Broad-spectrum sunscreen lotion with a comfortable finish, made for daily outdoor protection.",
                        "4.5" },
                { "Under Eye Gel Cream", "620", "14",
                        "Cooling eye gel that helps hydrate the under-eye area and reduce the look of tiredness.",
                        "4.2" },
                { "Cleansing Balm 100g", "720", "16",
                        "Soft cleansing balm that melts makeup and sunscreen, leaving skin clean before a second cleanse.",
                        "4.5" },
                { "Micellar Cleansing Water", "390", "33",
                        "Gentle micellar water for removing light makeup, sunscreen, and daily impurities without rinsing.",
                        "4.4" },
                { "Charcoal Nose Strip Pack", "250", "40",
                        "Easy-use charcoal nose strips designed to lift surface buildup and leave the nose area feeling cleaner.",
                        "4.1" },
                { "Keratin Smooth Shampoo", "560", "31",
                        "Smoothing shampoo for frizz-prone hair, helping cleanse while leaving strands softer and easier to manage.",
                        "4.5" },
                { "Argan Oil Conditioner", "620", "28",
                        "Nourishing conditioner with argan oil feel for softer lengths, smoother combing, and healthy-looking shine.",
                        "4.6" },
                { "Hair Repair Mask 250ml", "690", "17",
                        "Rich hair mask for dry or damaged hair, adding slip, softness, and a smoother after-wash feel.",
                        "4.4" },
                { "Body Lotion Shea 400ml", "520", "26",
                        "Creamy shea body lotion for daily moisture, leaving arms and legs comfortable without a sticky finish.",
                        "4.5" },
                { "Hand Cream Trio", "480", "21",
                        "Three compact hand creams with soft scents, useful for bags, desks, travel, and daily hydration.",
                        "4.3" },
                { "Nail Polish Classic Set", "590", "18",
                        "Classic nail polish set with easy shades, glossy finish, and colors suitable for everyday styling.",
                        "4.2" },
                { "Makeup Brush Kit 12pcs", "1250", "10",
                        "Complete brush kit for base, eyes, and blending, packed for beginners and regular makeup users.",
                        "4.6" },
                { "Compact Powder Natural", "540", "25",
                        "Soft compact powder that helps set makeup, reduce shine, and keep the face fresh through the day.",
                        "4.4" },
                { "Liquid Foundation Medium", "890", "13",
                        "Blendable liquid foundation with medium coverage and a natural finish for daily or occasion makeup.",
                        "4.3" },
                { "Waterproof Mascara Black", "460", "30",
                        "Black waterproof mascara designed for lifted lashes, definition, and reliable wear in humid weather.",
                        "4.4" },
                { "Perfume Mist Floral 150ml", "640", "19",
                        "Light floral perfume mist for everyday freshness, layering, and quick touch-ups after travel or work.",
                        "4.5" } });

        seedCategory("womens-clothes", new String[][] {
                { "Printed Cotton Kurti", "1250", "16",
                        "Breathable cotton kurti with a neat printed finish, comfortable for everyday wear and casual outings.",
                        "4.6" },
                { "Classic Black Handbag", "1750", "9",
                        "Structured black handbag with a spacious interior, sturdy handles, and a polished everyday look.",
                        "4.4" },
                { "Embroidered Lawn Three Piece", "2490", "12",
                        "Soft lawn three-piece set with delicate embroidery, matching dupatta, and comfortable seasonal wearability.",
                        "4.7" },
                { "Solid Rayon Kurti", "990", "22",
                        "Simple rayon kurti with a relaxed cut, easy drape, and styling flexibility for office or casual wear.",
                        "4.3" },
                { "Denim Long Tunic", "1850", "11",
                        "Long denim tunic with a structured feel, front buttons, and a modern casual silhouette.",
                        "4.4" },
                { "Floral Maxi Dress", "2190", "15",
                        "Flowy floral maxi dress with soft movement, breathable fabric, and an easy outfit-ready design.",
                        "4.5" },
                { "Modest Abaya Black", "2850", "8",
                        "Elegant black abaya with a clean shape, comfortable fabric, and simple detailing for everyday modest wear.",
                        "4.6" },
                { "Pleated Chiffon Hijab", "420", "34",
                        "Lightweight chiffon hijab with soft pleats, easy styling, and a polished finish for daily use.",
                        "4.5" },
                { "Cotton Palazzo Pants", "790", "26",
                        "Comfortable cotton palazzo pants with a relaxed leg, elastic waist, and easy pairing options.",
                        "4.3" },
                { "High Waist Denim Jeans", "1690", "14",
                        "High waist denim jeans with a flattering fit, everyday wash, and practical five-pocket styling.",
                        "4.4" },
                { "Soft Knit Cardigan", "1590", "10",
                        "Soft knit cardigan for layering over dresses, kurtis, or tops during cooler evenings.",
                        "4.5" },
                { "Office Blazer Beige", "2750", "7",
                        "Tailored beige blazer with clean lines, useful for office outfits, meetings, and smart casual styling.",
                        "4.6" },
                { "Satin Scarf Printed", "540", "30",
                        "Printed satin scarf with a smooth finish, suitable for hair styling, neck styling, or bag accents.",
                        "4.2" },
                { "Everyday Tote Bag", "1350", "17",
                        "Roomy tote bag with a practical inner pocket, strong straps, and enough space for daily essentials.",
                        "4.5" },
                { "Crossbody Mini Bag", "1180", "21",
                        "Compact crossbody bag with adjustable strap and organized pockets for phone, cards, and small essentials.",
                        "4.4" },
                { "Comfort Sandals Tan", "1450", "13",
                        "Tan comfort sandals with cushioned sole, easy straps, and a versatile look for daily walking.",
                        "4.3" },
                { "Block Heel Shoes", "1890", "9",
                        "Stable block heel shoes with a refined shape, useful for office wear, events, and evenings.",
                        "4.4" },
                { "Canvas Sneakers White", "1650", "18",
                        "White canvas sneakers with a clean look, lightweight feel, and easy styling with casual outfits.",
                        "4.5" },
                { "Printed Cotton Saree", "2350", "12",
                        "Light printed cotton saree with a soft drape, comfortable for daytime events and regular wear.",
                        "4.6" },
                { "Muslin Saree Pastel", "3250", "6",
                        "Pastel muslin saree with a graceful drape, delicate texture, and elegant occasion-ready appearance.",
                        "4.7" },
                { "Layered Necklace Set", "690", "24",
                        "Layered necklace set with a minimal finish, suitable for kurtis, dresses, and everyday outfits.",
                        "4.2" },
                { "Gold Tone Bangle Pack", "780", "20",
                        "Gold-tone bangle pack with mixed textures, designed for festive styling and daily accessories.",
                        "4.3" },
                { "Pearl Stud Earrings", "390", "38",
                        "Simple pearl stud earrings with a clean finish, easy to wear for office, casual, or occasion looks.",
                        "4.5" },
                { "Travel Makeup Pouch", "520", "28",
                        "Compact makeup pouch with a wipeable lining and organized space for beauty and travel essentials.",
                        "4.4" },
                { "Sleepwear Cotton Set", "1490", "16",
                        "Soft cotton sleepwear set with a relaxed fit, breathable feel, and comfort for warm nights.",
                        "4.6" } });

        seedCategory("electronics-devices", new String[][] {
                { "Wireless Earbuds", "2190", "20",
                        "Compact wireless earbuds with pocket case, clear call quality, and reliable sound for daily listening.",
                        "4.7" },
                { "Fast Charging Power Bank", "2690", "11",
                        "Portable 20000mAh power bank with fast charging support for phones, earbuds, and small devices.",
                        "4.8" },
                { "Bluetooth Neckband", "1590", "18",
                        "Lightweight Bluetooth neckband with stable connection, comfortable fit, and long battery life for travel.",
                        "4.5" },
                { "Smart Fitness Band", "2490", "13",
                        "Fitness band with step tracking, heart rate monitoring, sleep insights, and phone notification support.",
                        "4.4" },
                { "USB C Fast Charger 30W", "890", "31",
                        "Compact 30W USB C charger for faster phone charging, travel convenience, and daily desk use.",
                        "4.6" },
                { "Braided Type C Cable", "320", "45",
                        "Durable braided Type C cable with reinforced ends, suitable for charging and regular data transfer.",
                        "4.5" },
                { "Laptop Stand Foldable", "1250", "17",
                        "Foldable laptop stand that improves viewing angle, airflow, and desk comfort for work or study.",
                        "4.6" },
                { "Wireless Mouse Silent", "780", "28",
                        "Silent wireless mouse with smooth tracking, compact shape, and comfortable daily use for office work.",
                        "4.4" },
                { "Mechanical Keyboard Compact", "3890", "9",
                        "Compact mechanical keyboard with tactile keys, sturdy build, and efficient layout for typing and gaming.",
                        "4.7" },
                { "Portable Bluetooth Speaker", "2790", "14",
                        "Portable speaker with strong sound, rechargeable battery, and easy Bluetooth pairing for indoor or outdoor use.",
                        "4.5" },
                { "HD Webcam 1080p", "2290", "12",
                        "Full HD webcam with clear video quality, built-in microphone, and easy setup for meetings or classes.",
                        "4.4" },
                { "LED Desk Lamp", "1490", "21",
                        "Adjustable LED desk lamp with multiple brightness levels, useful for study, reading, and focused work.",
                        "4.6" },
                { "Smart Plug WiFi", "990", "19",
                        "WiFi smart plug for app-controlled appliances, scheduling, and convenient home automation basics.",
                        "4.3" },
                { "Mini Tripod Stand", "650", "26",
                        "Portable mini tripod for phones and small cameras, helpful for video calls, reels, and product photos.",
                        "4.4" },
                { "Phone Gimbal Stabilizer", "4590", "7",
                        "Handheld phone gimbal that keeps videos smoother while recording travel, events, or content.",
                        "4.5" },
                { "Gaming Headset Wired", "2190", "10",
                        "Wired gaming headset with cushioned earcups, clear microphone, and immersive sound for long sessions.",
                        "4.4" },
                { "Memory Card 128GB", "1150", "34",
                        "128GB memory card for phones, cameras, and action devices, suitable for photos, videos, and backups.",
                        "4.6" },
                { "USB Flash Drive 64GB", "620", "40",
                        "64GB USB flash drive with compact body, simple plug-and-play storage, and everyday file transfer use.",
                        "4.5" },
                { "HDMI Cable 2m", "420", "37",
                        "Two-meter HDMI cable for connecting laptops, consoles, monitors, and TVs with reliable signal quality.",
                        "4.4" },
                { "WiFi Router Dual Band", "3490", "8",
                        "Dual-band WiFi router for smoother browsing, streaming, and multiple home devices on one network.",
                        "4.5" },
                { "Portable SSD 512GB", "5890", "6",
                        "Fast portable SSD with 512GB storage for backups, work files, media projects, and travel use.",
                        "4.8" },
                { "Action Camera 4K", "6990", "5",
                        "Compact 4K action camera with wide-angle recording for trips, sports, and everyday adventure videos.",
                        "4.3" },
                { "Digital Kitchen Scale", "980", "23",
                        "Accurate digital kitchen scale for baking, meal prep, portion control, and everyday kitchen measuring.",
                        "4.5" },
                { "Electric Kettle 1.8L", "1650", "16",
                        "Large electric kettle with quick boiling, auto shutoff, and practical capacity for tea or coffee.",
                        "4.6" },
                { "Rechargeable Table Fan", "3290", "10",
                        "Rechargeable table fan with adjustable speed, portable handle, and backup comfort during warm weather.",
                        "4.5" } });
    }

    private void seedCategory(String categorySlug, String[][] items) {
        String[] images = demoImages(categorySlug);
        for (int i = 0; i < items.length; i++) {
            String[] item = items[i];
            seed(item[0], categorySlug, item[1], Integer.parseInt(item[2]), item[3], images[i % images.length],
                    Double.parseDouble(item[4]));
        }
    }

    private String[] demoImages(String categorySlug) {
        if ("cosmetics-skincare".equals(categorySlug)) {
            return new String[] {
                    "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=900&q=80" };
        }
        if ("womens-clothes".equals(categorySlug)) {
            return new String[] {
                    "https://images.unsplash.com/photo-1618244972963-dbee1a7edc95?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=900&q=80" };
        }
        if ("electronics-devices".equals(categorySlug)) {
            return new String[] {
                    "https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80",
                    "https://images.unsplash.com/photo-1550009158-9ebf69173e03?auto=format&fit=crop&w=900&q=80" };
        }
        return new String[] {
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1515543904379-3d757afe72e4?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?auto=format&fit=crop&w=900&q=80" };
    }

    private void seed(String name, String categorySlug, String price, int stock, String description, String imageUrl,
            double rating) {
        Category category = findCategory(categorySlug).orElse(categories().get(0));
        long id = productIds.incrementAndGet();
        productRepository.save(new Product(id, name, Product.slugify(name), category.slug(), category.name(),
                new BigDecimal(price), stock, description, imageUrl, rating, false,
                LocalDateTime.now().minusDays(productIds.get() % 7)));
    }

    private String detectCategorySlug(String query) {
        String value = normalize(query);
        if (value.matches(".*(serum|skin|cream|beauty|makeup|lip|hair|toner|sunscreen|perfume).*")) {
            return "cosmetics-skincare";
        }
        if (value.matches(".*(dress|kurti|bag|saree|shoe|cloth|jeans|hijab|scarf|girls|fashion).*")) {
            return "womens-clothes";
        }
        if (value.matches(".*(phone|charger|earbud|device|laptop|mouse|keyboard|camera|fan|speaker|electronic).*")) {
            return "electronics-devices";
        }
        return "grocery";
    }

    private BigDecimal suggestedPrice(String query) {
        int seed = Math.abs(normalize(query).hashCode());
        return BigDecimal.valueOf(250 + (seed % 4500));
    }

    private int suggestedStock(String query) {
        return 8 + (Math.abs(normalize(query).hashCode()) % 34);
    }

    private double suggestedRating(String query) {
        return 4.2 + (Math.abs(normalize(query).hashCode()) % 7) / 10.0;
    }

    private String suggestedImage(String categorySlug) {
        return demoImages(categorySlug)[0];
    }

    private Comparator<Product> comparator(String sort) {
        if ("priceAsc".equals(sort)) {
            return Comparator.comparing(Product::getPrice);
        }
        if ("priceDesc".equals(sort)) {
            return Comparator.comparing(Product::getPrice).reversed();
        }
        if ("rating".equals(sort)) {
            return Comparator.comparing(Product::getRating).reversed();
        }
        return Comparator.comparing(Product::getCreatedAt).reversed();
    }

    private String searchable(Product product) {
        return normalize(product.getName() + " " + product.getCategoryName() + " " + product.getDescription());
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private String titleCase(String value) {
        String normalized = value.trim().replaceAll("\\s+", " ");
        return java.util.Arrays.stream(normalized.split(" "))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }
}
