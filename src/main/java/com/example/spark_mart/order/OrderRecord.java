package com.example.spark_mart.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderRecord {
    @Id
    private String orderNumber;
    private Long customerId;
    private String name;
    private String email;
    private String phone;
    @Column(length = 1000)
    private String address;
    private String area;
    @Column(length = 1000)
    private String note;
    private String paymentMethod;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_number"))
    private List<OrderLine> lines = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal deliveryCharge;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private String paymentStatus;
    private String fulfillmentStatus;

    protected OrderRecord() {
    }

    public OrderRecord(String orderNumber, String name, String email, String phone, String address, String area,
            String note, String paymentMethod, List<OrderLine> lines, BigDecimal total) {
        this(orderNumber, null, name, email, phone, address, area, note, paymentMethod, lines, total);
    }

    public OrderRecord(String orderNumber, Long customerId, String name, String email, String phone, String address,
            String area, String note, String paymentMethod, List<OrderLine> lines, BigDecimal total) {
        this(orderNumber, customerId, name, email, phone, address, area, note, paymentMethod, lines, total,
                BigDecimal.valueOf(80), total.add(BigDecimal.valueOf(80)));
    }

    public OrderRecord(String orderNumber, Long customerId, String name, String email, String phone, String address,
            String area, String note, String paymentMethod, List<OrderLine> lines, BigDecimal subtotal,
            BigDecimal deliveryCharge, BigDecimal total) {
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.area = area;
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.lines = new ArrayList<>(lines);
        this.subtotal = subtotal;
        this.deliveryCharge = deliveryCharge;
        this.total = total;
        this.createdAt = LocalDateTime.now();
        this.paymentStatus = "CONFIRMED";
        this.fulfillmentStatus = "Order Sent";
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getArea() {
        return area;
    }

    public String getNote() {
        return note;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String paymentLabel() {
        return switch (paymentMethod) {
            case "bkash" -> "bKash";
            case "nagad" -> "Nagad";
            case "card" -> "Credit/Debit Card";
            case "cod", "cash" -> "Cash on Delivery";
            default -> "Cash on Delivery";
        };
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public BigDecimal getSubtotal() {
        return subtotal == null ? total : subtotal;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge == null ? BigDecimal.ZERO : deliveryCharge;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(String fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public void updateDeliveryInfo(String phone, String address, String area, String note) {
        if (phone != null && !phone.isBlank()) {
            this.phone = phone.trim();
        }
        if (address != null && !address.isBlank()) {
            this.address = address.trim();
        }
        this.area = area == null ? this.area : area.trim();
        this.note = note == null ? this.note : note.trim();
    }
}
