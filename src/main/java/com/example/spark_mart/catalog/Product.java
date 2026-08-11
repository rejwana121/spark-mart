package com.example.spark_mart.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private long id;
    private String name;
    private String slug;
    private String categorySlug;
    private String categoryName;

    // Real JPA relationship (Many-to-Many): a product can have many tags, and a
    // tag can belong to many products. Hibernate manages the join table for us.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_tags",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(precision = 12, scale = 2)
    private BigDecimal price;
    private int stock;
    @Column(length = 1200)
    private String description;
    @Column(length = 1000)
    private String imageUrl;
    private double rating;
    private boolean aiSuggested;
    private LocalDateTime createdAt;

    protected Product() {
    }

    public Product(long id, String name, String slug, String categorySlug, String categoryName, BigDecimal price,
            int stock, String description, String imageUrl, double rating, boolean aiSuggested, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.categorySlug = categorySlug;
        this.categoryName = categoryName;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.aiSuggested = aiSuggested;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public boolean isAiSuggested() {
        return aiSuggested;
    }

    public boolean isAIGenerated() {
        return aiSuggested;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isInStock() {
        return stock > 0;
    }

    public String getStockLabel() {
        if (stock <= 0) {
            return "Out of stock";
        }
        return stock + " in stock";
    }

    public void updateFrom(ProductForm form, String categoryName) {
        this.name = form.getName();
        this.slug = slugify(form.getName());
        this.categorySlug = form.getCategorySlug();
        this.categoryName = categoryName;
        this.price = form.getPrice();
        this.stock = form.getStock();
        this.description = form.getDescription();
        this.imageUrl = form.getImageUrl();
        this.rating = form.getRating();
        this.aiSuggested = form.isAiSuggested();
    }

    public void adjustStock(int quantityDelta) {
        this.stock = Math.max(0, this.stock + quantityDelta);
    }

    public static String slugify(String value) {
        return value == null || value.isBlank()
                ? "product"
                : value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
