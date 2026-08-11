package com.example.spark_mart.cart;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.spark_mart.catalog.CatalogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {
    private final CatalogService catalogService;
    private final CartService cartService;

    public CartController(CatalogService catalogService, CartService cartService) {
        this.catalogService = catalogService;
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        model.addAttribute("cart", cartService.getCart(session));
        return "cart";
    }

    @PostMapping("/cart/add")
    public String add(@RequestParam long productId, @RequestParam(defaultValue = "1") int quantity,
            HttpSession session, HttpServletRequest request) {
        catalogService.findProduct(productId).ifPresent(product -> {
            cartService.getCart(session).add(product, quantity);
            cartService.saveCart(session);
        });
        return "redirect:" + backTo(request);
    }


    @PostMapping("/cart/buy-now")
    public String buyNow(@RequestParam long productId, @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {
        catalogService.findProduct(productId).ifPresent(product -> {
            cartService.getCart(session).clear();
            cartService.getCart(session).add(product, quantity);
            cartService.saveCart(session);
        });
        return "redirect:/checkout";
    }

    @PostMapping("/cart/add-ai")
    public String addAi(@RequestParam String query, HttpSession session) {
        cartService.getCart(session).add(catalogService.aiSuggestionFor(query), 1);
        cartService.saveCart(session);
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String update(@RequestParam long productId, @RequestParam int quantity, HttpSession session) {
        cartService.getCart(session).update(productId, quantity);
        cartService.saveCart(session);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String remove(@RequestParam long productId, HttpSession session) {
        cartService.getCart(session).remove(productId);
        cartService.saveCart(session);
        return "redirect:/cart";
    }

    private String backTo(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return referer == null || referer.isBlank() ? "/" : referer;
    }
}
