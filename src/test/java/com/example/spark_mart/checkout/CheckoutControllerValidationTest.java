package com.example.spark_mart.checkout;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.spark_mart.cart.CartService;
import com.example.spark_mart.cart.ShoppingCart;
import com.example.spark_mart.customer.CustomerService;
import com.example.spark_mart.order.OrderService;

class CheckoutControllerValidationTest {

    private MockMvc mockMvc;
    private CartService cartService;
    private OrderService orderService;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        orderService = mock(OrderService.class);
        customerService = mock(CustomerService.class);
        CheckoutController controller = new CheckoutController(cartService, orderService, customerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getCheckoutWithEmptyCartRedirectsToCart() throws Exception {
        when(cartService.getCart(any())).thenReturn(new ShoppingCart());

        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void postCheckoutWithCompleteFormRedirectsToPayment() throws Exception {
        ShoppingCart cart = new ShoppingCart();
        com.example.spark_mart.catalog.Product p = new com.example.spark_mart.catalog.Product(
                1L, "Test", "test", "grocery", "Grocery",
                java.math.BigDecimal.valueOf(100), 5, "Desc", "img.jpg", 4.5, false,
                java.time.LocalDateTime.now());
        cart.add(p, 1);
        when(cartService.getCart(any())).thenReturn(cart);
        when(customerService.currentCustomer(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/checkout")
                        .param("name", "John Doe")
                        .param("email", "john@test.com")
                        .param("phone", "01700000000")
                        .param("address", "123 Main St")
                        .param("area", "Dhaka")
                        .param("consent", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment"));
    }

    @Test
    void getPaymentWithoutCheckoutFormRedirectsToCheckout() throws Exception {
        ShoppingCart cart = new ShoppingCart();
        com.example.spark_mart.catalog.Product p = new com.example.spark_mart.catalog.Product(
                1L, "Test", "test", "grocery", "Grocery",
                java.math.BigDecimal.valueOf(100), 5, "Desc", "img.jpg", 4.5, false,
                java.time.LocalDateTime.now());
        cart.add(p, 1);
        when(cartService.getCart(any())).thenReturn(cart);

        mockMvc.perform(get("/payment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout"));
    }

    @Test
    void getPaymentWithEmptyCartRedirectsToCart() throws Exception {
        when(cartService.getCart(any())).thenReturn(new ShoppingCart());

        mockMvc.perform(get("/payment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void postPaymentConfirmWithEmptyCartRedirectsToCart() throws Exception {
        when(cartService.getCart(any())).thenReturn(new ShoppingCart());

        mockMvc.perform(post("/payment/confirm")
                        .param("paymentMethod", "cod"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }
}
