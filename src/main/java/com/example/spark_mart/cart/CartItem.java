package com.example.spark_mart.cart;

import java.math.BigDecimal;

import com.example.spark_mart.catalog.Product;

public class CartItem {
    private final long productId;
    private final String name;
    private final BigDecimal unitPrice;
    private final String imageUrl;
    private final boolean aiSuggested;
    private final int availableStock;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.productId = product.getId();
        this.name = product.getName();
        this.unitPrice = product.getPrice();
        this.imageUrl = product.getImageUrl();
        this.aiSuggested = product.isAiSuggested();
        this.availableStock = Math.max(0, product.getStock());
        setQuantity(quantity);
    }

    public long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isAiSuggested() {
        return aiSuggested;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        int requested = Math.max(1, quantity);
        this.quantity = availableStock > 0 ? Math.min(requested, availableStock) : 0;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
