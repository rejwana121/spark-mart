package com.example.spark_mart.order;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.spark_mart.customer.CustomerService;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final CustomerService customerService;

    public OrderController(OrderService orderService, CustomerService customerService) {
        this.orderService = orderService;
        this.customerService = customerService;
    }

    @GetMapping("/order/{orderNumber}")
    public String confirmation(@PathVariable String orderNumber, Model model) {
        OrderRecord order = orderService.find(orderNumber).orElseThrow();
        model.addAttribute("order", order);
        return "order-confirmation";
    }

    @GetMapping("/order/success/{orderNumber}")
    public String success(@PathVariable String orderNumber, Model model) {
        return confirmation(orderNumber, model);
    }

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        Long customerId = customerService.currentCustomer(session).orElseThrow().getId();
        model.addAttribute("orders", orderService.listForCustomer(customerId));
        return "orders";
    }

    @GetMapping("/orders/{orderNumber}")
    public String orderDetails(@PathVariable String orderNumber, HttpSession session, Model model) {
        Long customerId = customerService.currentCustomer(session).orElseThrow().getId();
        OrderRecord order = orderService.find(orderNumber)
                .filter(record -> customerId.equals(record.getCustomerId()))
                .orElseThrow();
        model.addAttribute("order", order);
        return "order-confirmation";
    }


    @PostMapping("/orders/{orderNumber}/cancel")
    public String cancelOrder(@PathVariable String orderNumber, HttpSession session) {
        Long customerId = customerService.currentCustomer(session).orElseThrow().getId();
        orderService.cancelForCustomer(orderNumber, customerId);
        return "redirect:/orders/" + orderNumber;
    }

    @PostMapping("/orders/{orderNumber}/update")
    public String updateOrder(@PathVariable String orderNumber, @RequestParam String phone,
            @RequestParam String address, @RequestParam(required = false) String area,
            @RequestParam(required = false) String note, HttpSession session) {
        Long customerId = customerService.currentCustomer(session).orElseThrow().getId();
        orderService.updateDeliveryForCustomer(orderNumber, customerId, phone, address, area, note);
        customerService.updateProfile(customerId, null, phone, address, area, note);
        return "redirect:/orders/" + orderNumber;
    }

    @PostMapping("/track-order")
    public String track(@RequestParam String orderNumber, @RequestParam String contact, Model model) {
        model.addAttribute("orderNumber", orderNumber);
        model.addAttribute("contact", contact);
        model.addAttribute("order", orderService.track(orderNumber, contact).orElse(null));
        model.addAttribute("notFound", orderService.track(orderNumber, contact).isEmpty());
        return "track-order";
    }
}
