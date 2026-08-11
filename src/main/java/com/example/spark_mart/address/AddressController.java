package com.example.spark_mart.address;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.spark_mart.customer.CustomerService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AddressController {

    private final AddressService addressService;
    private final CustomerService customerService;

    public AddressController(AddressService addressService, CustomerService customerService) {
        this.addressService = addressService;
        this.customerService = customerService;
    }

    @GetMapping("/addresses")
    public String addresses(HttpSession session, Model model) {
        Long customerId = customerService.currentCustomer(session).orElseThrow().getId();
        model.addAttribute("addresses", addressService.listForCustomer(customerId));
        return "addresses";
    }

    @PostMapping("/addresses/add")
    public String add(@RequestParam String label, @RequestParam String addressLine,
            @RequestParam(required = false) String area, @RequestParam(required = false) String phone,
            HttpSession session) {
        Long customerId = customerService.currentCustomer(session).orElseThrow().getId();
        addressService.add(customerId, label, addressLine, area, phone);
        return "redirect:/addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Long customerId = customerService.currentCustomer(session).orElseThrow().getId();
        addressService.delete(id, customerId);
        return "redirect:/addresses";
    }
}
