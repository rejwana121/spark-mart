package com.example.spark_mart.cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartRecord, Long> {
    Optional<CartRecord> findByCustomerId(Long customerId);
}
