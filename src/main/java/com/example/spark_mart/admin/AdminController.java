package com.example.spark_mart.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.spark_mart.catalog.CatalogService;
import com.example.spark_mart.catalog.ProductForm;
import com.example.spark_mart.customer.CustomerService;
import com.example.spark_mart.order.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {
    private final AdminService adminService;
    private final CatalogService catalogService;
    private final OrderService orderService;
    private final CustomerService customerService;

    public AdminController(AdminService adminService, CatalogService catalogService, OrderService orderService,
            CustomerService customerService) {
        this.adminService = adminService;
        this.catalogService = catalogService;
        this.orderService = orderService;
        this.customerService = customerService;
    }

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String doLogin(@RequestParam String username, @RequestParam String password, HttpSession session,
            Model model) {
        if (adminService.authenticate(username, password)) {
            session.removeAttribute(com.example.spark_mart.customer.CustomerService.SESSION_CUSTOMER_ID);
            session.removeAttribute(AdminService.SESSION_SELLER);
            session.setAttribute(AdminService.SESSION_ADMIN, username);
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("error", "Invalid username or password.");
        return "admin/login";
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(AdminService.SESSION_ADMIN);
        session.removeAttribute(AdminService.SESSION_SELLER);
        session.removeAttribute(com.example.spark_mart.customer.CustomerService.SESSION_CUSTOMER_ID);
        return "redirect:/";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", catalogService.allProducts().size());
        model.addAttribute("lowStockCount", catalogService.lowStockCount());
        model.addAttribute("onDemandCount", catalogService.onDemandCount());
        model.addAttribute("totalOrders", orderService.totalOrders());
        model.addAttribute("totalRevenue", orderService.totalRevenue());
        model.addAttribute("newOrderNotifications", orderService.notificationCount());
        model.addAttribute("recentOrders", orderService.listNewestFirst().stream().limit(6).toList());
        return "admin/dashboard";
    }

    @GetMapping("/admin/products")
    public String products(Model model) {
        model.addAttribute("products", catalogService.allProducts());
        return "admin/products";
    }

    @GetMapping("/admin/products/new")
    public String newProduct(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", catalogService.categories());
        model.addAttribute("tags", catalogService.allTags());
        model.addAttribute("formAction", "/admin/products");
        return "admin/product-form";
    }

    @PostMapping("/admin/products")
    public String createProduct(@ModelAttribute ProductForm productForm) {
        catalogService.save(productForm);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{id}/edit")
    public String editProduct(@PathVariable long id, Model model) {
        return catalogService.findProductForm(id).map(productForm -> {
            model.addAttribute("productForm", productForm);
            model.addAttribute("categories", catalogService.categories());
            model.addAttribute("tags", catalogService.allTags());
            model.addAttribute("formAction", "/admin/products/" + id);
            model.addAttribute("productId", id);
            return "admin/product-form";
        }).orElse("redirect:/admin/products");
    }

    @PostMapping({ "/admin/products/{id}", "/admin/products/{id}/update" })
    public String updateProduct(@PathVariable long id, @ModelAttribute ProductForm productForm) {
        catalogService.update(id, productForm);
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/stock")
    public String adjustStock(@PathVariable long id, @RequestParam int quantityDelta) {
        catalogService.adjustStock(id, quantityDelta);
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/delete")
    public String deleteProduct(@PathVariable long id) {
        catalogService.delete(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.listNewestFirst());
        return "admin/orders";
    }

    @PostMapping("/admin/orders/{orderNumber}/status")
    public String updateOrderStatus(@PathVariable String orderNumber, @RequestParam String fulfillmentStatus,
            @RequestParam String paymentStatus) {
        orderService.updateStatus(orderNumber, fulfillmentStatus, paymentStatus);
        return "redirect:/admin/orders";
    }

    @GetMapping("/admin/categories")
    public String categories(Model model) {
        model.addAttribute("categories", catalogService.categories());
        return "admin/categories";
    }

    @PostMapping("/admin/categories")
    public String saveCategory(@RequestParam String slug, @RequestParam String name, @RequestParam String description) {
        catalogService.saveCategory(slug, name, description);
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/{slug}/delete")
    public String deleteCategory(@PathVariable String slug) {
        catalogService.deleteCategory(slug);
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/customers")
    public String customers(Model model) {
        model.addAttribute("customers", customerService.listCustomers());
        return "admin/customers";
    }

    @GetMapping("/admin/payments")
    public String payments(Model model) {
        model.addAttribute("payments", orderService.payments());
        return "admin/payments";
    }

    @GetMapping("/admin/analytics")
    public String analytics(Model model) {
        model.addAttribute("totalOrders", orderService.totalOrders());
        model.addAttribute("totalRevenue", orderService.totalRevenue());
        model.addAttribute("totalCustomers", customerService.listCustomers().size());
        model.addAttribute("topProducts", orderService.soldQuantityByProduct());
        model.addAttribute("categoryRevenue", orderService.revenueByCategory(catalogService));
        model.addAttribute("products", catalogService.allProducts());
        return "admin/analytics";
    }

    @GetMapping("/admin/inventory")
    public String inventory(Model model) {
        model.addAttribute("products", catalogService.allProducts());
        model.addAttribute("lowStockCount", catalogService.lowStockCount());
        return "admin/inventory";
    }

    @GetMapping({ "/admin/ai-sales-prediction", "/admin/predictions" })
    public String predictions(Model model) {
        model.addAttribute("soldByProduct", orderService.soldQuantityByProduct());
        model.addAttribute("products", catalogService.allProducts());
        model.addAttribute("categoryRevenue", orderService.revenueByCategory(catalogService));
        return "admin/ai-sales-prediction";
    }
}