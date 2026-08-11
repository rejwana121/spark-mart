package com.example.spark_mart.cart;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.spark_mart.catalog.Product;

public class ShoppingCart {
    private static final BigDecimal DELIVERY_CHARGE = BigDecimal.valueOf(80);
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public void add(Product product, int quantity) {
        if (!product.isInStock()) {
            return;
        }
        CartItem item = items.get(product.getId());
        if (item == null) {
            items.put(product.getId(), new CartItem(product, Math.max(1, quantity)));
        } else {
            item.setQuantity(item.getQuantity() + Math.max(1, quantity));
        }
    }

    public void update(long productId, int quantity) {
        if (quantity <= 0) {
            items.remove(productId);
            return;
        }
        CartItem item = items.get(productId);
        if (item != null) {
            item.setQuantity(quantity);
        }
    }

    public void remove(long productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        return items.values().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDeliveryCharge() {
        return isEmpty() ? BigDecimal.ZERO : DELIVERY_CHARGE;
    }

    public BigDecimal getGrandTotal() {
        return getSubtotal().add(getDeliveryCharge());
    }
}
