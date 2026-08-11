package com.example.spark_mart.customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerUser, Long> {
    Optional<CustomerUser> findByEmailIgnoreCase(String email);
}
