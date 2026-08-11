package com.example.spark_mart.order;

import java.math.BigDecimal;

import com.example.spark_mart.cart.CartItem;

import jakarta.persistence.Embeddable;

@Embeddable
public class OrderLine {
    private Long productId;
    private String name;
    private String imageUrl;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private boolean aiSuggested;

    protected OrderLine() {
    }

    public OrderLine(Long productId, String name, String imageUrl, int quantity, BigDecimal unitPrice,
            BigDecimal lineTotal, boolean aiSuggested) {
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.aiSuggested = aiSuggested;
    }

    public static OrderLine from(CartItem item) {
        return new OrderLine(item.getProductId(), item.getName(), item.getImageUrl(), item.getQuantity(),
                item.getUnitPrice(), item.getLineTotal(), item.isAiSuggested());
    }

    public Long productId() { return productId; }
    public String name() { return name; }
    public String imageUrl() { return imageUrl; }
    public int quantity() { return quantity; }
    public BigDecimal unitPrice() { return unitPrice; }
    public BigDecimal lineTotal() { return lineTotal; }
    public boolean aiSuggested() { return aiSuggested; }
}
