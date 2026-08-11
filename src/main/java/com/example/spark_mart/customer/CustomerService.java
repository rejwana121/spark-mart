package com.example.spark_mart.customer;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.spark_mart.seller.Seller;
import com.example.spark_mart.seller.SellerRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class CustomerService {
    public static final String SESSION_CUSTOMER_ID = "sparkMartCustomerId";

    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, SellerRepository sellerRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.sellerRepository = sellerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<CustomerUser> currentCustomer(HttpSession session) {
        Object id = session.getAttribute(SESSION_CUSTOMER_ID);
        if (id instanceof Long customerId) {
            return customerRepository.findById(customerId);
        }
        return Optional.empty();
    }

    public Optional<CustomerUser> findByEmail(String email) {
        return customerRepository.findByEmailIgnoreCase(email == null ? "" : email.trim());
    }

    public CustomerUser register(String name, String email, String phone, String password, String confirmPassword) {
        return register(name, email, phone, password, confirmPassword, null, null, null);
    }

    public CustomerUser register(String name, String email, String phone, String password, String confirmPassword,
            String address, String area, String orderNote) {
        validateAccountFields(name, email, password, confirmPassword);
        customerRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            throw new IllegalArgumentException("Email already registered.");
        });
        CustomerUser customer = new CustomerUser(name.trim(), email.trim(), phone, passwordEncoder.encode(password));
        customer.updateProfile(name, phone, address, area, orderNote);
        return customerRepository.save(customer);
    }

    public CustomerUser register(String name, String email, String password) {
        return register(name, email, null, password, password);
    }

    public CustomerUser registerSeller(String storeName, String name, String email, String phone, String password,
            String confirmPassword, String address, String area) {
        validateAccountFields(name, email, password, confirmPassword);
        if (storeName == null || storeName.isBlank()) {
            throw new IllegalArgumentException("Store name is required for seller account.");
        }
        Optional<CustomerUser> existing = customerRepository.findByEmailIgnoreCase(email.trim());
        CustomerUser seller;
        if (existing.isPresent()) {
            seller = existing.get();
            if (!passwordEncoder.matches(password, seller.getPassword())) {
                throw new IllegalArgumentException("This email already has a customer account. Use the same password to upgrade it to seller.");
            }
            seller.updateProfile(name, phone, address, area, seller.getOrderNote());
        } else {
            seller = new CustomerUser(name.trim(), email.trim(), phone, passwordEncoder.encode(password));
            seller.updateProfile(name, phone, address, area, null);
        }
        seller.becomeSeller(storeName, phone, address, area);
        CustomerUser savedCustomer = customerRepository.save(seller);
        Seller sellerRecord = sellerRepository.findByCustomerId(savedCustomer.getId())
                .orElse(new Seller(savedCustomer.getId(), storeName));
        sellerRecord.setStoreName(storeName);
        sellerRecord.setCustomerId(savedCustomer.getId());
        sellerRepository.save(sellerRecord);
        return savedCustomer;
    }

    public Optional<CustomerUser> authenticate(String email, String password) {
        return customerRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .filter(customer -> passwordEncoder.matches(password, customer.getPassword()));
    }

    public void login(HttpSession session, CustomerUser customer) {
        session.setAttribute(SESSION_CUSTOMER_ID, customer.getId());
    }

    public void logout(HttpSession session) {
        session.removeAttribute(SESSION_CUSTOMER_ID);
    }

    public List<CustomerUser> listCustomers() {
        return customerRepository.findAll();
    }

    public List<CustomerUser> listSellers() {
        return customerRepository.findAll().stream().filter(CustomerUser::isSeller).toList();
    }

    public CustomerUser updateProfile(Long customerId, String name, String phone, String address) {
        return updateProfile(customerId, name, phone, address, null, null);
    }

    public CustomerUser updateProfile(Long customerId, String name, String phone, String address, String area,
            String orderNote) {
        CustomerUser customer = customerRepository.findById(customerId).orElseThrow();
        customer.updateProfile(name, phone, address, area, orderNote);
        return customerRepository.save(customer);
    }

    private void validateAccountFields(String name, String email, String password, String confirmPassword) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Confirm password must match.");
        }
    }
}
