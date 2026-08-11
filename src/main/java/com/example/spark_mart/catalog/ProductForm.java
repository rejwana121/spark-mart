package com.example.spark_mart.catalog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductForm {
    private String name;
    private String categorySlug;
    private BigDecimal price = BigDecimal.ZERO;
    private int stock;
    private String description;
    private String imageUrl;
    private double rating = 4.5;
    private boolean aiSuggested;
    private List<Long> tagIds = new ArrayList<>();

    public static ProductForm from(Product product) {
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setCategorySlug(product.getCategorySlug());
        form.setPrice(product.getPrice());
        form.setStock(product.getStock());
        form.setDescription(product.getDescription());
        form.setImageUrl(product.getImageUrl());
        form.setRating(product.getRating());
        form.setAiSuggested(product.isAiSuggested());
        form.setTagIds(product.getTags().stream().map(Tag::getId).toList());
        return form;
    }

    public Product toProduct(long id, String categoryName) {
        return new Product(id, name, Product.slugify(name), categorySlug, categoryName, price, stock, description,
                imageUrl, rating, aiSuggested, java.time.LocalDateTime.now());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price == null ? BigDecimal.ZERO : price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isAiSuggested() {
        return aiSuggested;
    }

    public void setAiSuggested(boolean aiSuggested) {
        this.aiSuggested = aiSuggested;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds == null ? new ArrayList<>() : tagIds;
    }
}
