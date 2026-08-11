package com.example.spark_mart.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.example.spark_mart.customer.CustomerService;

class CustomerAuthenticationInterceptorTest {

    private CustomerAuthenticationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new CustomerAuthenticationInterceptor();
    }

    @Test
    void customerSessionPassesCustomerInterceptor() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute(CustomerService.SESSION_CUSTOMER_ID, 1L);

        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    @Test
    void noSessionRedirectsToLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(302, response.getStatus());
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    void adminOnlySessionIsRejectedByCustomerInterceptor() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute(com.example.spark_mart.admin.AdminService.SESSION_ADMIN, "admin@test.com");

        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(302, response.getStatus());
        assertEquals("/login", response.getRedirectedUrl());
    }
}
