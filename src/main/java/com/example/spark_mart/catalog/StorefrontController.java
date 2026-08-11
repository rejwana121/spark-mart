package com.example.spark_mart.catalog;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StorefrontController {
    private final CatalogService catalogService;

    public StorefrontController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String q, @RequestParam(required = false) String sort,
            Model model) {
        Map<String, List<Product>> productsByCategory = new LinkedHashMap<>();
        Map<String, String> categoryImages = new LinkedHashMap<>();
        List<Category> categories = catalogService.categories();
        categories.forEach(category -> {
            List<Product> categoryProducts = catalogService.filter(category.slug(), null, null, null, false, 0, "rating");
            productsByCategory.put(category.slug(), categoryProducts);
            categoryImages.put(category.slug(), categoryProducts.isEmpty()
                    ? "https://images.unsplash.com/photo-1556742044-3c52d6e88c62?auto=format&fit=crop&w=400&q=80"
                    : categoryProducts.get(0).getImageUrl());
        });

        String searchQuery = q == null ? "" : q.trim();
        List<Product> searchResults = searchQuery.isBlank()
                ? List.of()
                : catalogService.filter(null, searchQuery, null, null, false, 0, sort);
        Product generatedProduct = null;
        if (!searchQuery.isBlank() && searchResults.isEmpty()) {
            generatedProduct = catalogService.aiSuggestionFor(searchQuery);
            searchResults = List.of(generatedProduct);
        }

        model.addAttribute("featuredProducts", catalogService.featuredProducts());
        model.addAttribute("newArrivals", catalogService.newArrivals());
        model.addAttribute("categories", categories);
        model.addAttribute("productsByCategory", productsByCategory);
        model.addAttribute("categoryImages", categoryImages);
        model.addAttribute("justForYou", catalogService.allProducts().stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .toList());
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("generatedProduct", generatedProduct);
        model.addAttribute("sort", sort);
        return "home";
    }

    @GetMapping("/category/{slug}")
    public String category(@PathVariable String slug, @RequestParam(required = false) String min,
            @RequestParam(required = false) String max, @RequestParam(required = false) String stock,
            @RequestParam(required = false) String rating, @RequestParam(required = false) String sort, Model model) {
        Category category = catalogService.findCategory(slug).orElseThrow();
        List<Product> products = catalogService.filter(slug, null, parseMoney(min), parseMoney(max),
                "true".equals(stock), parseRating(rating), sort);
        model.addAttribute("category", category);
        model.addAttribute("products", products);
        model.addAttribute("sort", sort);
        model.addAttribute("min", min);
        model.addAttribute("max", max);
        model.addAttribute("stock", stock);
        model.addAttribute("rating", rating);
        return "category";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", catalogService.categories());
        return "categories";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, @RequestParam(required = false) String sort,
            RedirectAttributes redirectAttributes) {
        if (q != null && !q.isBlank()) {
            redirectAttributes.addAttribute("q", q.trim());
        }
        if (sort != null && !sort.isBlank()) {
            redirectAttributes.addAttribute("sort", sort);
        }
        return "redirect:/";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable long id, Model model) {
        Product product = catalogService.findProduct(id).orElseThrow();
        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", catalogService.filter(product.getCategorySlug(), null, null, null, false,
                0, "rating").stream().filter(related -> related.getId() != product.getId()).limit(6).toList());
        return "product";
    }

    @GetMapping("/track-order")
    public String trackOrder() {
        return "track-order";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    private BigDecimal parseMoney(String value) {
        try {
            return value == null || value.isBlank() ? null : new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private double parseRating(String value) {
        try {
            return value == null || value.isBlank() ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
