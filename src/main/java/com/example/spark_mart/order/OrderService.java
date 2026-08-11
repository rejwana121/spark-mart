package com.example.spark_mart.order;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.example.spark_mart.cart.CartItem;
import com.example.spark_mart.cart.ShoppingCart;
import com.example.spark_mart.catalog.CatalogService;
import com.example.spark_mart.checkout.CheckoutForm;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CatalogService catalogService;

    public OrderService(OrderRepository orderRepository, PaymentRepository paymentRepository, CatalogService catalogService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.catalogService = catalogService;
    }

    public OrderRecord createOrder(CheckoutForm form, ShoppingCart cart) {
        return createOrder(form, cart, null);
    }

    public OrderRecord createOrder(CheckoutForm form, ShoppingCart cart, Long customerId) {
        return createOrder(form, cart, customerId, null);
    }

    public OrderRecord createOrder(CheckoutForm form, ShoppingCart cart, Long customerId, String transactionId) {
        validateStock(cart);
        String orderNumber = nextOrderNumber();
        List<OrderLine> lines = cart.getItems().stream().map(OrderLine::from).toList();
        OrderRecord order = new OrderRecord(orderNumber, customerId, form.getName(), form.getEmail(), form.getPhone(),
                form.getAddress(), form.getArea(), form.getNote(), form.getPaymentMethod(), lines, cart.getSubtotal(),
                cart.getDeliveryCharge(), cart.getGrandTotal());
        if ("cod".equals(form.getPaymentMethod()) || "cash".equals(form.getPaymentMethod())) {
            order.setPaymentStatus("PENDING");
        }
        OrderRecord saved = orderRepository.save(order);
        reduceStock(cart);
        paymentRepository.save(new PaymentRecord(orderNumber, form.getPaymentMethod(), saved.getPaymentStatus(), transactionId,
                saved.getTotal()));
        return saved;
    }

    public Optional<OrderRecord> find(String orderNumber) {
        return orderRepository.findById(normalizeOrderNumber(orderNumber));
    }

    public Optional<OrderRecord> track(String orderNumber, String contact) {
        String normalizedContact = contact == null ? "" : contact.trim();
        return find(orderNumber)
                .filter(order -> order.getPhone().equals(normalizedContact)
                        || (order.getEmail() != null && order.getEmail().equalsIgnoreCase(normalizedContact)));
    }

    public List<OrderRecord> listNewestFirst() {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(OrderRecord::getCreatedAt).reversed())
                .toList();
    }

    public List<OrderRecord> listForCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public void updateStatus(String orderNumber, String fulfillmentStatus, String paymentStatus) {
        find(orderNumber).ifPresent(order -> {
            if (fulfillmentStatus != null && !fulfillmentStatus.isBlank()) {
                String normalized = normalizeFulfillmentStatus(fulfillmentStatus);
                if (isAllowedTransition(order.getFulfillmentStatus(), normalized)) {
                    order.setFulfillmentStatus(normalized);
                }
            }
            if (paymentStatus != null && !paymentStatus.isBlank()) {
                order.setPaymentStatus(paymentStatus);
            }
            orderRepository.save(order);
        });
    }

    public boolean cancelForCustomer(String orderNumber, Long customerId) {
        Optional<OrderRecord> found = find(orderNumber)
                .filter(order -> customerId != null && customerId.equals(order.getCustomerId()));
        if (found.isEmpty()) {
            return false;
        }
        OrderRecord order = found.get();
        if (!isAllowedTransition(order.getFulfillmentStatus(), "Cancelled")) {
            return false;
        }
        order.setFulfillmentStatus("Cancelled");
        orderRepository.save(order);
        return true;
    }

    public boolean updateDeliveryForCustomer(String orderNumber, Long customerId, String phone, String address,
            String area, String note) {
        Optional<OrderRecord> found = find(orderNumber)
                .filter(order -> customerId != null && customerId.equals(order.getCustomerId()));
        if (found.isEmpty()) {
            return false;
        }
        OrderRecord order = found.get();
        if (!List.of("Order Sent", "Approved").contains(order.getFulfillmentStatus())) {
            return false;
        }
        order.updateDeliveryInfo(phone, address, area, note);
        orderRepository.save(order);
        return true;
    }

    public long totalOrders() {
        return orderRepository.count();
    }

    public java.math.BigDecimal totalRevenue() {
        return orderRepository.findAll().stream()
                .map(OrderRecord::getTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    public long notificationCount() {
        return orderRepository.findAll().stream().filter(order -> "Order Sent".equals(order.getFulfillmentStatus()))
                .count();
    }

    public List<PaymentRecord> payments() {
        return paymentRepository.findAll();
    }

    public Map<String, Integer> soldQuantityByProduct() {
        return orderRepository.findAll().stream()
                .flatMap(order -> order.getLines().stream())
                .collect(java.util.stream.Collectors.groupingBy(OrderLine::name,
                        java.util.stream.Collectors.summingInt(OrderLine::quantity)));
    }

    public Map<String, java.math.BigDecimal> revenueByCategory(com.example.spark_mart.catalog.CatalogService catalogService) {
        return orderRepository.findAll().stream()
                .flatMap(order -> order.getLines().stream())
                .collect(java.util.stream.Collectors.groupingBy(line -> catalogService.findProduct(line.productId())
                        .map(com.example.spark_mart.catalog.Product::getCategoryName).orElse("Unknown"),
                        java.util.stream.Collectors.reducing(java.math.BigDecimal.ZERO, OrderLine::lineTotal,
                                java.math.BigDecimal::add)));
    }

    private String nextOrderNumber() {
        return "SPK-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private String normalizeOrderNumber(String orderNumber) {
        return orderNumber == null ? "" : orderNumber.trim().toUpperCase();
    }

    private void validateStock(ShoppingCart cart) {
        for (CartItem item : cart.getItems()) {
            int currentStock = catalogService.findProduct(item.getProductId())
                    .map(com.example.spark_mart.catalog.Product::getStock)
                    .orElse(0);
            if (currentStock < item.getQuantity()) {
                throw new IllegalStateException(item.getName() + " has only " + currentStock + " item(s) in stock.");
            }
        }
    }

    private void reduceStock(ShoppingCart cart) {
        cart.getItems().forEach(item -> catalogService.adjustStock(item.getProductId(), -item.getQuantity()));
    }

    private String normalizeFulfillmentStatus(String status) {
        return switch (status) {
            case "ORDER_SENT", "Order Sent" -> "Order Sent";
            case "APPROVED", "Approved" -> "Approved";
            case "REJECTED", "Rejected" -> "Rejected";
            case "PROCESSING", "Processing" -> "Processing";
            case "DELIVERED", "Delivered" -> "Delivered";
            case "CANCELLED", "Cancelled" -> "Cancelled";
            default -> status;
        };
    }

    private boolean isAllowedTransition(String currentStatus, String nextStatus) {
        if (currentStatus == null || currentStatus.equals(nextStatus)) {
            return true;
        }
        return switch (currentStatus) {
            case "Order Sent" -> List.of("Approved", "Rejected", "Cancelled").contains(nextStatus);
            case "Approved" -> "Processing".equals(nextStatus);
            case "Processing" -> "Delivered".equals(nextStatus);
            default -> false;
        };
    }
}
