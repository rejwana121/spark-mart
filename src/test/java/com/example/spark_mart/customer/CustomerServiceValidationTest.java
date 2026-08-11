package com.example.spark_mart.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.spark_mart.seller.SellerRepository;

class CustomerServiceValidationTest {

    private CustomerRepository customerRepository;
    private SellerRepository sellerRepository;
    private PasswordEncoder passwordEncoder;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        sellerRepository = mock(SellerRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenAnswer(inv -> "hashed-" + inv.getArgument(0));
        when(passwordEncoder.matches(any(), any())).thenAnswer(inv -> {
            String raw = inv.getArgument(0);
            String hashed = inv.getArgument(1);
            return ("hashed-" + raw).equals(hashed);
        });
        customerService = new CustomerService(customerRepository, sellerRepository, passwordEncoder);
    }

    @Test
    void registerRequiresName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> customerService.register("", "a@b.com", "1234", "pass", "pass"));
        assertEquals("Name is required.", ex.getMessage());
    }

    @Test
    void registerRequiresEmail() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> customerService.register("John", "", "1234", "pass", "pass"));
        assertEquals("Email is required.", ex.getMessage());
    }

    @Test
    void registerRequiresPassword() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> customerService.register("John", "a@b.com", "1234", "", ""));
        assertEquals("Password is required.", ex.getMessage());
    }

    @Test
    void registerRequiresMatchingConfirmPassword() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> customerService.register("John", "a@b.com", "1234", "pass", "different"));
        assertEquals("Confirm password must match.", ex.getMessage());
    }

    @Test
    void registerSellerRequiresStoreName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> customerService.registerSeller("", "John", "a@b.com", "1234", "pass", "pass", null, null));
        assertEquals("Store name is required for seller account.", ex.getMessage());
    }

    @Test
    void authenticateMatchesCorrectPassword() {
        CustomerUser user = new CustomerUser("John", "john@test.com", "01700000000", "hashed-pass");
        when(customerRepository.findByEmailIgnoreCase("john@test.com")).thenReturn(Optional.of(user));

        Optional<CustomerUser> result = customerService.authenticate("john@test.com", "pass");

        assertTrue(result.isPresent());
        assertEquals("john@test.com", result.get().getEmail());
    }

    @Test
    void authenticateRejectsWrongPassword() {
        CustomerUser user = new CustomerUser("John", "john@test.com", "01700000000", "hashed-pass");
        when(customerRepository.findByEmailIgnoreCase("john@test.com")).thenReturn(Optional.of(user));

        Optional<CustomerUser> result = customerService.authenticate("john@test.com", "wrong");

        assertFalse(result.isPresent());
    }

    @Test
    void authenticateRejectsUnknownEmail() {
        when(customerRepository.findByEmailIgnoreCase("unknown@test.com")).thenReturn(Optional.empty());

        Optional<CustomerUser> result = customerService.authenticate("unknown@test.com", "pass");

        assertFalse(result.isPresent());
    }

    @Test
    void authenticateHandlesNullEmail() {
        Optional<CustomerUser> result = customerService.authenticate(null, "pass");

        assertFalse(result.isPresent());
    }
}
