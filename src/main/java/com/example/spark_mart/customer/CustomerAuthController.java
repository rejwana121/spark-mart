package com.example.spark_mart.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.spark_mart.order.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CustomerAuthController {
    private final CustomerService customerService;
    private final OrderService orderService;

    public CustomerAuthController(CustomerService customerService, OrderService orderService) {
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("mode", "login");
        return "auth";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        return customerService.authenticate(email, password)
                .map(customer -> {
                    customerService.login(session, customer);
                    if (customer.isSeller()) {
                        session.setAttribute(com.example.spark_mart.admin.AdminService.SESSION_ADMIN, customer.getEmail());
                    }
                    return "redirect:/";
                })
                .orElseGet(() -> {
                    model.addAttribute("mode", "login");
                    model.addAttribute("error", "Invalid email or password.");
                    return "auth";
                });
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("mode", "register");
        return "auth";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String name, @RequestParam String email,
            @RequestParam(required = false) String phone, @RequestParam String password,
            @RequestParam String confirmPassword, @RequestParam(required = false) String address,
            @RequestParam(required = false) String area, @RequestParam(required = false) String orderNote,
            HttpSession session, Model model) {
        try {
            CustomerUser customer = customerService.register(name, email, phone, password, confirmPassword, address, area,
                    orderNote);
            customerService.login(session, customer);
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("mode", "register");
            model.addAttribute("error", ex.getMessage());
            return "auth";
        }
    }

    @GetMapping("/seller/register")
    public String sellerRegister(Model model) {
        model.addAttribute("mode", "seller");
        return "auth";
    }

    @PostMapping("/seller/register")
    public String doSellerRegister(@RequestParam String storeName, @RequestParam String name,
            @RequestParam String email, @RequestParam(required = false) String phone, @RequestParam String password,
            @RequestParam String confirmPassword, @RequestParam(required = false) String address,
            @RequestParam(required = false) String area, HttpSession session, Model model) {
        try {
            CustomerUser seller = customerService.registerSeller(storeName, name, email, phone, password, confirmPassword,
                    address, area);
            customerService.login(session, seller);
            session.setAttribute(com.example.spark_mart.admin.AdminService.SESSION_ADMIN, seller.getEmail());
            return "redirect:/admin/dashboard";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("mode", "seller");
            model.addAttribute("error", ex.getMessage());
            return "auth";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        customerService.logout(session);
        session.removeAttribute(com.example.spark_mart.admin.AdminService.SESSION_ADMIN);
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        CustomerUser customer = customerService.currentCustomer(session).orElseThrow();
        var orders = orderService.listForCustomer(customer.getId());
        model.addAttribute("customer", customer);
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalSpent", orders.stream().map(com.example.spark_mart.order.OrderRecord::getTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        model.addAttribute("recentOrders", orders.stream().limit(5).toList());
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name, @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address, @RequestParam(required = false) String area,
            @RequestParam(required = false) String orderNote, HttpSession session) {
        Long customerId = customerService.currentCustomer(session).orElseThrow().getId();
        customerService.updateProfile(customerId, name, phone, address, area, orderNote);
        return "redirect:/profile";
    }
}
