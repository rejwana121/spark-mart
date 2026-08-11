package com.example.spark_mart.seller;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private String storeName;

    private String status = "ACTIVE";

    private LocalDateTime sellerSince = LocalDateTime.now();

    public Seller() {
    }

    public Seller(Long customerId, String storeName) {
        this.customerId = customerId;
        this.storeName = storeName;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSellerSince() {
        return sellerSince;
    }

    public void setSellerSince(LocalDateTime sellerSince) {
        this.sellerSince = sellerSince;
    }
}
