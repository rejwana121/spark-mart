package com.example.spark_mart.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.spark_mart.admin.AdminService;
import com.example.spark_mart.cart.CartService;
import com.example.spark_mart.catalog.CatalogService;
import com.example.spark_mart.customer.CustomerService;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalModelAdvice {
    private final CatalogService catalogService;
    private final CartService cartService;
    private final CustomerService customerService;

    public GlobalModelAdvice(CatalogService catalogService, CartService cartService, CustomerService customerService) {
        this.catalogService = catalogService;
        this.cartService = cartService;
        this.customerService = customerService;
    }

    @ModelAttribute("navCategories")
    public Object navCategories() {
        return catalogService.categories();
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        return cartService.getCart(session).getItemCount();
    }

    @ModelAttribute("miniCart")
    public Object miniCart(HttpSession session) {
        return cartService.getCart(session);
    }

    @ModelAttribute("currentCustomer")
    public Object currentCustomer(HttpSession session) {
        return customerService.currentCustomer(session).orElse(null);
    }

    @ModelAttribute("sellerLoggedIn")
    public boolean sellerLoggedIn(HttpSession session) {
        return session.getAttribute(AdminService.SESSION_SELLER) != null;
    }

    @ModelAttribute("sellerEmail")
    public Object sellerEmail(HttpSession session) {
        return session.getAttribute(AdminService.SESSION_SELLER);
    }

    @ModelAttribute("currentSeller")
    public Object currentSeller(HttpSession session) {
        Object email = session.getAttribute(AdminService.SESSION_SELLER);
        if (email instanceof String sellerEmail) {
            return customerService.findByEmail(sellerEmail).filter(com.example.spark_mart.customer.CustomerUser::isSeller)
                    .orElse(null);
        }
        return null;
    }
}
