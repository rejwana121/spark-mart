package com.example.spark_mart.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.spark_mart.catalog.CatalogService;
import com.example.spark_mart.customer.CustomerService;
import com.example.spark_mart.order.OrderService;
import com.example.spark_mart.security.AdminAuthenticationInterceptor;
import com.example.spark_mart.security.SellerAuthenticationInterceptor;

class SellerLoginRegressionTest {

    private MockMvc mockMvc;
    private AdminService adminService;
    private CatalogService catalogService;
    private OrderService orderService;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        catalogService = mock(CatalogService.class);
        orderService = mock(OrderService.class);
        customerService = mock(CustomerService.class);

        AdminController controller = new AdminController(adminService, catalogService, orderService, customerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void adminLoginStillWorks() throws Exception {
        when(adminService.authenticate("admin@sparkmart.com", "admin123")).thenReturn(true);

        mockMvc.perform(post("/admin/login")
                        .param("username", "admin@sparkmart.com")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(request().sessionAttribute(AdminService.SESSION_ADMIN, org.hamcrest.Matchers.notNullValue()))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER));

        verify(customerService, never()).authenticate(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void adminLoginClearsSellerSession() throws Exception {
        when(adminService.authenticate("admin@sparkmart.com", "admin123")).thenReturn(true);

        mockMvc.perform(post("/admin/login")
                        .param("username", "admin@sparkmart.com")
                        .param("password", "admin123")
                        .sessionAttr(AdminService.SESSION_SELLER, "seller@sparkmart.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER));
    }

    @Test
    void adminLoginWithInvalidCredentialsReturnsError() throws Exception {
        when(adminService.authenticate("wrong", "wrong")).thenReturn(false);

        mockMvc.perform(post("/admin/login")
                        .param("username", "wrong")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER));
    }

    @Test
    void sellerCredentialsRejectedByAdminLoginEndpoint() throws Exception {
        when(adminService.authenticate("seller@sparkmart.com", "pass123")).thenReturn(false);

        mockMvc.perform(post("/admin/login")
                        .param("username", "seller@sparkmart.com")
                        .param("password", "pass123"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN));
    }

    @Test
    void sellerCannotAccessAdminDashboardViaAdminInterceptor() throws Exception {
        AdminAuthenticationInterceptor interceptor = new AdminAuthenticationInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute(AdminService.SESSION_SELLER, "seller@sparkmart.com");

        boolean allowed = interceptor.preHandle(request, response, null);

        assertFalse(allowed);
        assertEquals(302, response.getStatus());
        assertEquals("/admin/login", response.getRedirectedUrl());
    }

    @Test
    void sellerCanAccessSellerDashboardViaSellerInterceptor() throws Exception {
        SellerAuthenticationInterceptor interceptor = new SellerAuthenticationInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute(AdminService.SESSION_SELLER, "seller@sparkmart.com");

        boolean allowed = interceptor.preHandle(request, response, null);

        assertTrue(allowed);
        assertEquals(200, response.getStatus());
    }

    @Test
    void sellerLogoutPreventsSellerDashboardAccess() throws Exception {
        SellerAuthenticationInterceptor interceptor = new SellerAuthenticationInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, null);

        assertFalse(allowed);
        assertEquals(302, response.getStatus());
        assertEquals("/seller/login", response.getRedirectedUrl());
    }

    @Test
    void adminSessionPassesAdminAuthenticationInterceptor() throws Exception {
        AdminAuthenticationInterceptor interceptor = new AdminAuthenticationInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute(AdminService.SESSION_ADMIN, "admin@sparkmart.com");

        boolean allowed = interceptor.preHandle(request, response, null);

        assertTrue(allowed);
        assertEquals(200, response.getStatus());
    }

    @Test
    void adminLogoutClearsAllSessions() throws Exception {
        mockMvc.perform(post("/admin/logout")
                        .sessionAttr(AdminService.SESSION_ADMIN, "admin@sparkmart.com")
                        .sessionAttr(AdminService.SESSION_SELLER, "seller@test.com")
                        .sessionAttr(com.example.spark_mart.customer.CustomerService.SESSION_CUSTOMER_ID, 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER))
                .andExpect(request().sessionAttributeDoesNotExist(com.example.spark_mart.customer.CustomerService.SESSION_CUSTOMER_ID));
    }
}
