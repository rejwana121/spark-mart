package com.example.spark_mart.cart;

import jakarta.persistence.Embeddable;

@Embeddable
public class CartLine {
    private Long productId;
    private int quantity;

    protected CartLine() {
    }

    public CartLine(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = Math.max(1, quantity);
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
