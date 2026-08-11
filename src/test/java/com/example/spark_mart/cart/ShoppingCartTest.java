package com.example.spark_mart.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.spark_mart.catalog.Product;

class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    private Product product(long id, String name, int stock, BigDecimal price) {
        return new Product(id, name, "slug-" + id, "grocery", "Grocery", price, stock,
                "Description", "img.jpg", 4.5, false, LocalDateTime.now());
    }

    @Test
    void emptyCartHasZeroItemsAndZeroTotal() {
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItemCount());
        assertEquals(BigDecimal.ZERO, cart.getSubtotal());
        assertEquals(BigDecimal.ZERO, cart.getDeliveryCharge());
        assertEquals(BigDecimal.ZERO, cart.getGrandTotal());
    }

    @Test
    void addingInStockProductIncreasesItemCount() {
        Product p = product(1L, "Rice", 10, BigDecimal.valueOf(500));

        cart.add(p, 2);

        assertFalse(cart.isEmpty());
        assertEquals(2, cart.getItemCount());
        assertEquals(BigDecimal.valueOf(1000), cart.getSubtotal());
    }

    @Test
    void addingOutOfStockProductIsIgnored() {
        Product p = product(2L, "OOS Item", 0, BigDecimal.valueOf(200));

        cart.add(p, 1);

        assertTrue(cart.isEmpty());
    }

    @Test
    void addingSameProductTwiceIncreasesQuantity() {
        Product p = product(3L, "Lentils", 20, BigDecimal.valueOf(100));

        cart.add(p, 2);
        cart.add(p, 3);

        assertEquals(5, cart.getItemCount());
    }

    @Test
    void addQuantityIsClampedToAvailableStock() {
        Product p = product(4L, "Oil", 3, BigDecimal.valueOf(300));

        cart.add(p, 10);

        assertEquals(3, cart.getItemCount());
    }

    @Test
    void updateQuantityChangesItemQuantity() {
        Product p = product(5L, "Tea", 50, BigDecimal.valueOf(80));
        cart.add(p, 1);

        cart.update(p.getId(), 5);

        assertEquals(5, cart.getItemCount());
        assertEquals(BigDecimal.valueOf(400), cart.getSubtotal());
    }

    @Test
    void updateQuantityToZeroOrNegativeRemovesItem() {
        Product p = product(6L, "Coffee", 10, BigDecimal.valueOf(200));
        cart.add(p, 2);

        cart.update(p.getId(), 0);

        assertTrue(cart.isEmpty());
    }

    @Test
    void removeItemRemovesItFromCart() {
        Product p = product(7L, "Sugar", 15, BigDecimal.valueOf(60));
        cart.add(p, 3);

        cart.remove(p.getId());

        assertTrue(cart.isEmpty());
    }

    @Test
    void clearRemovesAllItems() {
        cart.add(product(8L, "A", 10, BigDecimal.valueOf(100)), 1);
        cart.add(product(9L, "B", 10, BigDecimal.valueOf(200)), 1);

        cart.clear();

        assertTrue(cart.isEmpty());
    }

    @Test
    void deliveryChargeIsAppliedWhenCartIsNotEmpty() {
        cart.add(product(10L, "Item", 5, BigDecimal.valueOf(50)), 1);

        assertEquals(BigDecimal.valueOf(80), cart.getDeliveryCharge());
        assertEquals(BigDecimal.valueOf(130), cart.getGrandTotal());
    }

    @Test
    void deliveryChargeIsZeroWhenCartIsEmpty() {
        assertEquals(BigDecimal.ZERO, cart.getDeliveryCharge());
    }

    @Test
    void grandTotalCombinesSubtotalAndDelivery() {
        Product p = product(11L, "Expensive", 5, BigDecimal.valueOf(1000));
        cart.add(p, 2);

        BigDecimal expectedSubtotal = BigDecimal.valueOf(2000);
        BigDecimal expectedDelivery = BigDecimal.valueOf(80);
        assertEquals(expectedSubtotal.add(expectedDelivery), cart.getGrandTotal());
    }
}
