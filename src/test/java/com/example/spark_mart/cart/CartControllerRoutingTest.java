package com.example.spark_mart.cart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.spark_mart.catalog.CatalogService;
import com.example.spark_mart.catalog.Product;

class CartControllerRoutingTest {

    private MockMvc mockMvc;
    private CatalogService catalogService;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        catalogService = mock(CatalogService.class);
        cartService = mock(CartService.class);
        CartController controller = new CartController(catalogService, cartService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Product testProduct() {
        return new Product(1L, "Test Product", "test-product", "grocery", "Grocery",
                BigDecimal.valueOf(500), 10, "Description", "img.jpg", 4.5, false, LocalDateTime.now());
    }

    @Test
    void addProductRedirectsBackToReferer() throws Exception {
        when(catalogService.findProduct(1L)).thenReturn(Optional.of(testProduct()));
        when(cartService.getCart(any())).thenReturn(new ShoppingCart());

        mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .param("quantity", "2")
                        .header("Referer", "/product/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/1"));
    }

    @Test
    void addProductRedirectsHomeWithoutReferer() throws Exception {
        when(catalogService.findProduct(1L)).thenReturn(Optional.of(testProduct()));
        when(cartService.getCart(any())).thenReturn(new ShoppingCart());

        mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void buyNowRedirectsToCheckout() throws Exception {
        when(catalogService.findProduct(1L)).thenReturn(Optional.of(testProduct()));
        when(cartService.getCart(any())).thenReturn(new ShoppingCart());

        mockMvc.perform(post("/cart/buy-now")
                        .param("productId", "1")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout"));
    }

    @Test
    void updateRedirectsToCart() throws Exception {
        when(cartService.getCart(any())).thenReturn(new ShoppingCart());

        mockMvc.perform(post("/cart/update")
                        .param("productId", "1")
                        .param("quantity", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void removeRedirectsToCart() throws Exception {
        when(cartService.getCart(any())).thenReturn(new ShoppingCart());

        mockMvc.perform(post("/cart/remove")
                        .param("productId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void addNonExistentProductDoesNotChangeCart() throws Exception {
        when(catalogService.findProduct(999L)).thenReturn(Optional.empty());
        ShoppingCart cart = new ShoppingCart();
        when(cartService.getCart(any())).thenReturn(cart);

        mockMvc.perform(post("/cart/add")
                        .param("productId", "999")
                        .param("quantity", "1")
                        .header("Referer", "/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(cartService, never()).saveCart(any());
    }
}
