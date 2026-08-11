package com.example.spark_mart.customer;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class CustomerUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    private String phone;
    @Column(nullable = false)
    private String password;
    @Column(length = 1000)
    private String address;
    private String area;
    @Column(length = 1000)
    private String orderNote;
    private String role = "CUSTOMER";
    private String sellerStoreName;
    private String sellerStatus;
    private LocalDateTime sellerSince;
    private Boolean enabled = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    protected CustomerUser() {
    }

    public CustomerUser(String name, String email, String password) {
        this(name, email, null, password);
    }

    public CustomerUser(String name, String email, String phone, String password) {
        this.name = cleanRequired(name);
        this.email = cleanRequired(email).toLowerCase();
        this.phone = clean(phone);
        this.password = password;
    }

    public Long getId() {
        return id;
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

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public String getArea() {
        return area;
    }

    public String getOrderNote() {
        return orderNote;
    }

    public String getRole() {
        return role;
    }

    public String getSellerStoreName() {
        return sellerStoreName;
    }

    public String getSellerStatus() {
        return sellerStatus;
    }

    public LocalDateTime getSellerSince() {
        return sellerSince;
    }

    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    public boolean isSeller() {
        return "SELLER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateProfile(String name, String phone, String address) {
        updateProfile(name, phone, address, this.area, this.orderNote);
    }

    public void updateProfile(String name, String phone, String address, String area, String orderNote) {
        this.name = name == null || name.isBlank() ? this.name : name.trim();
        this.phone = clean(phone);
        this.address = clean(address);
        this.area = clean(area);
        this.orderNote = clean(orderNote);
    }

    public void becomeSeller(String storeName, String phone, String address, String area) {
        this.role = "SELLER";
        this.sellerStoreName = storeName == null || storeName.isBlank()
                ? (this.name + " Store")
                : storeName.trim();
        this.sellerStatus = "ACTIVE";
        this.sellerSince = sellerSince == null ? LocalDateTime.now() : sellerSince;
        this.phone = clean(phone == null || phone.isBlank() ? this.phone : phone);
        this.address = clean(address == null || address.isBlank() ? this.address : address);
        this.area = clean(area == null || area.isBlank() ? this.area : area);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String cleanRequired(String value) {
        return value == null ? "" : value.trim();
    }
}
