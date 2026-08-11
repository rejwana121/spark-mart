package com.example.spark_mart.cart;

import org.springframework.stereotype.Service;

import com.example.spark_mart.catalog.CatalogService;

import jakarta.servlet.http.HttpSession;

@Service
public class CartService {
    private static final String SESSION_CART = "sparkMartCart";
    private final CatalogService catalogService;
    private final CartRepository cartRepository;

    public CartService(CatalogService catalogService, CartRepository cartRepository) {
        this.catalogService = catalogService;
        this.cartRepository = cartRepository;
    }

    public ShoppingCart getCart(HttpSession session) {
        Object existing = session.getAttribute(SESSION_CART);
        if (existing instanceof ShoppingCart cart) {
            return cart;
        }
        ShoppingCart cart = new ShoppingCart();
        currentCustomerId(session)
                .flatMap(cartRepository::findByCustomerId)
                .ifPresent(record -> record.getLines().forEach(line -> catalogService.findProduct(line.getProductId())
                        .ifPresent(product -> cart.add(product, line.getQuantity()))));
        session.setAttribute(SESSION_CART, cart);
        return cart;
    }

    public void saveCart(HttpSession session) {
        currentCustomerId(session).ifPresent(customerId -> {
            ShoppingCart cart = getCart(session);
            CartRecord record = cartRepository.findByCustomerId(customerId).orElseGet(() -> new CartRecord(customerId));
            record.getLines().clear();
            cart.getItems().stream()
                    .filter(item -> item.getQuantity() > 0)
                    .forEach(item -> record.getLines().add(new CartLine(item.getProductId(), item.getQuantity())));
            cartRepository.save(record);
        });
    }

    private java.util.Optional<Long> currentCustomerId(HttpSession session) {
        Object id = session.getAttribute(com.example.spark_mart.customer.CustomerService.SESSION_CUSTOMER_ID);
        return id instanceof Long customerId ? java.util.Optional.of(customerId) : java.util.Optional.empty();
    }
}
