package com.example.spark_mart.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderRecord, String> {
    List<OrderRecord> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
