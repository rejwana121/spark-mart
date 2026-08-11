package com.example.spark_mart.checkout;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.spark_mart.cart.CartService;
import com.example.spark_mart.cart.ShoppingCart;
import com.example.spark_mart.customer.CustomerService;
import com.example.spark_mart.order.OrderRecord;
import com.example.spark_mart.order.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {
    private final CartService cartService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private static final String SESSION_CHECKOUT_FORM = "sparkMartCheckoutForm";

    public CheckoutController(CartService cartService, OrderService orderService, CustomerService customerService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.customerService = customerService;
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        ShoppingCart cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        CheckoutForm form = new CheckoutForm();
        customerService.currentCustomer(session).ifPresent(customer -> {
            form.setName(customer.getName());
            form.setEmail(customer.getEmail());
            form.setPhone(customer.getPhone());
            form.setAddress(customer.getAddress());
            form.setArea(customer.getArea());
            form.setNote(customer.getOrderNote());
            form.setConsent(true);
        });
        model.addAttribute("checkoutForm", form);
        return "checkout";
    }

    @PostMapping({ "/checkout", "/checkout/info" })
    public String saveCustomerInfo(@ModelAttribute CheckoutForm checkoutForm, HttpSession session, Model model) {
        ShoppingCart cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        if (!checkoutForm.isComplete()) {
            model.addAttribute("cart", cart);
            model.addAttribute("checkoutForm", checkoutForm);
            model.addAttribute("error", "Please complete the required checkout fields and consent checkbox.");
            return "checkout";
        }
        customerService.currentCustomer(session).ifPresent(customer ->
                customerService.updateProfile(customer.getId(), checkoutForm.getName(), checkoutForm.getPhone(),
                        checkoutForm.getAddress(), checkoutForm.getArea(), checkoutForm.getNote()));
        session.setAttribute(SESSION_CHECKOUT_FORM, checkoutForm);
        return "redirect:/payment";
    }

    @GetMapping("/payment")
    public String payment(HttpSession session, Model model) {
        ShoppingCart cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        if (!(session.getAttribute(SESSION_CHECKOUT_FORM) instanceof CheckoutForm)) {
            return "redirect:/checkout";
        }
        model.addAttribute("cart", cart);
        return "payment";
    }

    @PostMapping({ "/payment", "/payment/confirm" })
    public String confirmPayment(@RequestParam String paymentMethod,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String cardholderName,
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String expiryDate,
            @RequestParam(required = false) String cvv,
            HttpSession session, Model model) {
        ShoppingCart cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        Object stored = session.getAttribute(SESSION_CHECKOUT_FORM);
        if (!(stored instanceof CheckoutForm checkoutForm)) {
            return "redirect:/checkout";
        }
        String validationError = validatePayment(paymentMethod, mobileNumber, transactionId, cardholderName, cardNumber,
                expiryDate, cvv);
        if (validationError != null) {
            model.addAttribute("cart", cart);
            model.addAttribute("error", validationError);
            return "payment";
        }
        checkoutForm.setPaymentMethod(paymentMethod);
        checkoutForm.setArea(checkoutForm.getArea() == null ? "" : checkoutForm.getArea());
        Long customerId = customerService.currentCustomer(session).map(customer -> customer.getId()).orElse(null);
        String savedTransactionId = "card".equals(paymentMethod) ? "CARD-" + last4(cardNumber)
                : ("cod".equals(paymentMethod) ? "COD" : transactionId);
        OrderRecord order;
        try {
            order = orderService.createOrder(checkoutForm, cart, customerId, savedTransactionId);
        } catch (IllegalStateException ex) {
            model.addAttribute("cart", cart);
            model.addAttribute("error", ex.getMessage());
            return "payment";
        }
        cart.clear();
        cartService.saveCart(session);
        session.removeAttribute(SESSION_CHECKOUT_FORM);
        return "redirect:/order/success/" + order.getOrderNumber();
    }

    private String validatePayment(String paymentMethod, String mobileNumber, String transactionId,
            String cardholderName, String cardNumber, String expiryDate, String cvv) {
        if ("cod".equals(paymentMethod)) {
            return null;
        }
        if ("bkash".equals(paymentMethod) || "nagad".equals(paymentMethod)) {
            if (isBlank(mobileNumber) || isBlank(transactionId)) {
                return "Please enter the mobile number and transaction ID.";
            }
            return null;
        }
        if ("card".equals(paymentMethod)) {
            if (isBlank(cardholderName) || isBlank(cardNumber) || isBlank(expiryDate) || isBlank(cvv)) {
                return "Please complete the card payment fields.";
            }
            return null;
        }
        return "Please select a valid payment method.";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String last4(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        return digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
    }
}
