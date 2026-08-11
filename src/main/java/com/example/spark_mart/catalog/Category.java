package com.example.spark_mart.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    private String slug;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, length = 500)
    private String description;

    protected Category() {
    }

    public Category(String slug, String name, String description) {
        this.slug = slug;
        this.name = name;
        this.description = description;
    }

    public String slug() {
        return slug;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }
}
