package com.example.spark_mart.seller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.spark_mart.admin.AdminService;
import com.example.spark_mart.catalog.CatalogService;
import com.example.spark_mart.catalog.ProductForm;
import com.example.spark_mart.customer.CustomerService;
import com.example.spark_mart.customer.CustomerUser;
import com.example.spark_mart.order.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SellerController {
    private final CatalogService catalogService;
    private final OrderService orderService;
    private final CustomerService customerService;

    public SellerController(CatalogService catalogService, OrderService orderService,
            CustomerService customerService) {
        this.catalogService = catalogService;
        this.orderService = orderService;
        this.customerService = customerService;
    }

    @GetMapping("/seller/login")
    public String login() {
        return "seller/login";
    }

    @PostMapping("/seller/login")
    public String doLogin(@RequestParam String email, @RequestParam String password, HttpSession session,
            org.springframework.ui.Model model) {
        return customerService.authenticate(email, password)
                .filter(CustomerUser::isSeller)
                .map(customer -> {
                    session.removeAttribute(AdminService.SESSION_ADMIN);
                    customerService.login(session, customer);
                    session.setAttribute(AdminService.SESSION_SELLER, customer.getEmail());
                    return "redirect:/seller/dashboard";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid email or password.");
                    return "seller/login";
                });
    }

    @GetMapping("/seller/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", catalogService.allProducts().size());
        model.addAttribute("lowStockCount", catalogService.lowStockCount());
        model.addAttribute("onDemandCount", catalogService.onDemandCount());
        model.addAttribute("totalOrders", orderService.totalOrders());
        model.addAttribute("totalRevenue", orderService.totalRevenue());
        model.addAttribute("newOrderNotifications", orderService.notificationCount());
        model.addAttribute("recentOrders", orderService.listNewestFirst().stream().limit(6).toList());
        return "seller/dashboard";
    }

    @GetMapping("/seller/products")
    public String products(Model model) {
        model.addAttribute("products", catalogService.allProducts());
        return "seller/products";
    }

    @GetMapping("/seller/products/new")
    public String newProduct(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", catalogService.categories());
        model.addAttribute("tags", catalogService.allTags());
        model.addAttribute("formAction", "/seller/products");
        return "seller/product-form";
    }

    @PostMapping("/seller/products")
    public String createProduct(@ModelAttribute ProductForm productForm) {
        catalogService.save(productForm);
        return "redirect:/seller/products";
    }

    @GetMapping("/seller/products/{id}/edit")
    public String editProduct(@PathVariable long id, Model model) {
        return catalogService.findProductForm(id).map(productForm -> {
            model.addAttribute("productForm", productForm);
            model.addAttribute("categories", catalogService.categories());
            model.addAttribute("tags", catalogService.allTags());
            model.addAttribute("formAction", "/seller/products/" + id);
            model.addAttribute("productId", id);
            return "seller/product-form";
        }).orElse("redirect:/seller/products");
    }

    @PostMapping({ "/seller/products/{id}", "/seller/products/{id}/update" })
    public String updateProduct(@PathVariable long id, @ModelAttribute ProductForm productForm) {
        catalogService.update(id, productForm);
        return "redirect:/seller/products";
    }

    @PostMapping("/seller/products/{id}/stock")
    public String adjustStock(@PathVariable long id, @RequestParam int quantityDelta) {
        catalogService.adjustStock(id, quantityDelta);
        return "redirect:/seller/products";
    }

    @PostMapping("/seller/products/{id}/delete")
    public String deleteProduct(@PathVariable long id) {
        catalogService.delete(id);
        return "redirect:/seller/products";
    }

    @GetMapping("/seller/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.listNewestFirst());
        return "seller/orders";
    }

    @PostMapping("/seller/orders/{orderNumber}/status")
    public String updateOrderStatus(@PathVariable String orderNumber, @RequestParam String fulfillmentStatus,
            @RequestParam String paymentStatus) {
        orderService.updateStatus(orderNumber, fulfillmentStatus, paymentStatus);
        return "redirect:/seller/orders";
    }

    @GetMapping("/seller/categories")
    public String categories(Model model) {
        model.addAttribute("categories", catalogService.categories());
        return "seller/categories";
    }

    @PostMapping("/seller/categories")
    public String saveCategory(@RequestParam String slug, @RequestParam String name,
            @RequestParam String description) {
        catalogService.saveCategory(slug, name, description);
        return "redirect:/seller/categories";
    }

    @PostMapping("/seller/categories/{slug}/delete")
    public String deleteCategory(@PathVariable String slug) {
        catalogService.deleteCategory(slug);
        return "redirect:/seller/categories";
    }

    @GetMapping("/seller/inventory")
    public String inventory(Model model) {
        model.addAttribute("products", catalogService.allProducts());
        model.addAttribute("lowStockCount", catalogService.lowStockCount());
        return "seller/inventory";
    }

    @PostMapping("/seller/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.removeAttribute(com.example.spark_mart.admin.AdminService.SESSION_SELLER);
        session.removeAttribute(com.example.spark_mart.customer.CustomerService.SESSION_CUSTOMER_ID);
        return "redirect:/";
    }
}
