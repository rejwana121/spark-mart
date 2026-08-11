package com.example.spark_mart.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;
    private String method;
    private String status;
    private String transactionId;
    private BigDecimal amount;
    private LocalDateTime paidAt = LocalDateTime.now();

    protected PaymentRecord() {
    }

    public PaymentRecord(String orderNumber, String method, String status, BigDecimal amount) {
        this(orderNumber, method, status, null, amount);
    }

    public PaymentRecord(String orderNumber, String method, String status, String transactionId, BigDecimal amount) {
        this.orderNumber = orderNumber;
        this.method = method;
        this.status = status;
        this.transactionId = transactionId;
        this.amount = amount;
    }

    public String getOrderNumber() { return orderNumber; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
    public String getTransactionId() { return transactionId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getPaidAt() { return paidAt; }
}
