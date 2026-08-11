package com.example.spark_mart.catalog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategorySlug(String categorySlug);

    Product findTopByOrderByIdDesc();
}
