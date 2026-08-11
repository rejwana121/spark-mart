package com.example.spark_mart.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.example.spark_mart.admin.AdminService;
import com.example.spark_mart.cart.CartService;
import com.example.spark_mart.catalog.CatalogService;
import com.example.spark_mart.config.GlobalModelAdvice;
import com.example.spark_mart.customer.CustomerService;
import com.example.spark_mart.customer.CustomerUser;

import jakarta.servlet.http.HttpSession;

class SessionAuthenticationTest {

    private AdminAuthenticationInterceptor adminInterceptor;
    private GlobalModelAdvice globalModelAdvice;
    private CustomerService customerService;
    private CartService cartService;
    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        adminInterceptor = new AdminAuthenticationInterceptor();
        customerService = mock(CustomerService.class);
        cartService = mock(CartService.class);
        catalogService = mock(CatalogService.class);
        globalModelAdvice = new GlobalModelAdvice(catalogService, cartService, customerService);
    }

    @Test
    void adminSessionPassesAdminAuthenticationInterceptor() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute(AdminService.SESSION_ADMIN, "admin@sparkmart.com");

        boolean result = adminInterceptor.preHandle(request, response, null);

        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    @Test
    void sellerOnlySessionIsRejectedByAdminAuthenticationInterceptor() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute(AdminService.SESSION_SELLER, "seller@sparkmart.com");

        boolean result = adminInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(302, response.getStatus());
        assertEquals("/admin/login", response.getRedirectedUrl());
    }

    @Test
    void customerOnlySessionIsRejectedByAdminAuthenticationInterceptor() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute(CustomerService.SESSION_CUSTOMER_ID, 1L);

        boolean result = adminInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(302, response.getStatus());
        assertEquals("/admin/login", response.getRedirectedUrl());
    }

    @Test
    void sellerIdentityIsReadFromSessionSellerByGlobalModelAdvice() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(AdminService.SESSION_SELLER)).thenReturn("seller@sparkmart.com");

        boolean loggedIn = globalModelAdvice.sellerLoggedIn(session);
        Object email = globalModelAdvice.sellerEmail(session);

        assertTrue(loggedIn);
        assertEquals("seller@sparkmart.com", email);
    }

    @Test
    void adminSessionDoesNotAppearAsSellerInGlobalModelAdvice() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(AdminService.SESSION_SELLER)).thenReturn(null);
        when(session.getAttribute(AdminService.SESSION_ADMIN)).thenReturn("admin@sparkmart.com");

        boolean loggedIn = globalModelAdvice.sellerLoggedIn(session);
        Object email = globalModelAdvice.sellerEmail(session);

        assertFalse(loggedIn);
        assertNull(email);
    }

    @Test
    void logoutCannotLeaveSessionAdminBehind() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(AdminService.SESSION_ADMIN)).thenAnswer(
                inv -> sessionAttributes.get(AdminService.SESSION_ADMIN));
        when(session.getAttribute(AdminService.SESSION_SELLER)).thenAnswer(
                inv -> sessionAttributes.get(AdminService.SESSION_SELLER));
        when(session.getAttribute(CustomerService.SESSION_CUSTOMER_ID)).thenAnswer(
                inv -> sessionAttributes.get(CustomerService.SESSION_CUSTOMER_ID));

        sessionAttributes.put(AdminService.SESSION_ADMIN, "admin@sparkmart.com");
        sessionAttributes.put(AdminService.SESSION_SELLER, "seller@sparkmart.com");
        sessionAttributes.put(CustomerService.SESSION_CUSTOMER_ID, 1L);

        session.removeAttribute(AdminService.SESSION_ADMIN);
        sessionAttributes.remove(AdminService.SESSION_ADMIN);
        session.removeAttribute(AdminService.SESSION_SELLER);
        sessionAttributes.remove(AdminService.SESSION_SELLER);
        session.removeAttribute(CustomerService.SESSION_CUSTOMER_ID);
        sessionAttributes.remove(CustomerService.SESSION_CUSTOMER_ID);

        assertNull(session.getAttribute(AdminService.SESSION_ADMIN));
        assertNull(session.getAttribute(AdminService.SESSION_SELLER));
        assertNull(session.getAttribute(CustomerService.SESSION_CUSTOMER_ID));
    }
}
