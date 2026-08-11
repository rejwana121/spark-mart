package com.example.spark_mart.admin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Verifies that AdminService authenticates using a real BCrypt hash comparison
 * (Chapter 5.6) rather than a plain-text equals() check.
 */
class AdminServiceTest {

    @Test
    void acceptsTheCorrectPassword() {
        AdminService adminService = new AdminService("admin@sparkmart.com", "admin123", new BCryptPasswordEncoder());

        assertTrue(adminService.authenticate("admin@sparkmart.com", "admin123"));
    }

    @Test
    void rejectsAWrongPassword() {
        AdminService adminService = new AdminService("admin@sparkmart.com", "admin123", new BCryptPasswordEncoder());

        assertFalse(adminService.authenticate("admin@sparkmart.com", "wrong-password"));
    }

    @Test
    void rejectsAnUnknownUsername() {
        AdminService adminService = new AdminService("admin@sparkmart.com", "admin123", new BCryptPasswordEncoder());

        assertFalse(adminService.authenticate("someone-else@sparkmart.com", "admin123"));
    }
}
