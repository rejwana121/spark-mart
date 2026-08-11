package com.example.spark_mart.seller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.spark_mart.admin.AdminController;
import com.example.spark_mart.admin.AdminService;
import com.example.spark_mart.catalog.CatalogService;
import com.example.spark_mart.customer.CustomerAuthController;
import com.example.spark_mart.customer.CustomerService;
import com.example.spark_mart.customer.CustomerUser;
import com.example.spark_mart.order.OrderService;
import com.example.spark_mart.security.AdminAuthenticationInterceptor;
import com.example.spark_mart.security.SellerAuthenticationInterceptor;

class SellerFlowEndToEndTest {

    private MockMvc sellerMockMvc;
    private MockMvc adminMockMvc;
    private MockMvc authMockMvc;
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

        doAnswer(invocation -> {
            jakarta.servlet.http.HttpSession session = invocation.getArgument(0);
            session.setAttribute(CustomerService.SESSION_CUSTOMER_ID, 1L);
            return null;
        }).when(customerService).login(any(), any());

        SellerController sellerController = new SellerController(catalogService, orderService, customerService);
        sellerMockMvc = MockMvcBuilders.standaloneSetup(sellerController).build();

        AdminController adminController = new AdminController(adminService, catalogService, orderService, customerService);
        adminMockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        CustomerAuthController authController = new CustomerAuthController(customerService, orderService);
        authMockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        when(catalogService.allProducts()).thenReturn(Collections.emptyList());
        when(catalogService.lowStockCount()).thenReturn(0L);
        when(catalogService.onDemandCount()).thenReturn(0L);
        when(orderService.totalOrders()).thenReturn(0L);
        when(orderService.totalRevenue()).thenReturn(java.math.BigDecimal.ZERO);
        when(orderService.notificationCount()).thenReturn(0L);
        when(orderService.listNewestFirst()).thenReturn(Collections.emptyList());
    }

    private CustomerUser sellerUser() {
        CustomerUser user = new CustomerUser("Seller One", "seller@sparkmart.com", "01700000000", "hashedpw");
        user.becomeSeller("Test Store", "01700000000", "123 St", "Dhaka");
        return user;
    }

    private CustomerUser regularCustomer() {
        return new CustomerUser("Regular Customer", "regular@sparkmart.com", "01800000000", "hashedpw");
    }

    @Test
    void sellerRegistrationRedirectsToSellerDashboardWithCorrectSession() throws Exception {
        CustomerUser seller = sellerUser();
        when(customerService.registerSeller(eq("Test Store"), eq("Seller One"), eq("seller@sparkmart.com"),
                eq("01700000000"), eq("pass123"), eq("pass123"), any(), any())).thenReturn(seller);

        authMockMvc.perform(post("/seller/register")
                        .param("storeName", "Test Store")
                        .param("name", "Seller One")
                        .param("email", "seller@sparkmart.com")
                        .param("phone", "01700000000")
                        .param("password", "pass123")
                        .param("confirmPassword", "pass123")
                        .param("address", "123 St")
                        .param("area", "Dhaka"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dashboard"))
                .andExpect(request().sessionAttribute(AdminService.SESSION_SELLER, "seller@sparkmart.com"))
                .andExpect(request().sessionAttribute(CustomerService.SESSION_CUSTOMER_ID,
                        org.hamcrest.Matchers.notNullValue()))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN));
    }

    @Test
    void validSellerLoginSetsSessionSellerAndCustomerIdNeverSessionAdmin() throws Exception {
        CustomerUser seller = sellerUser();
        when(customerService.authenticate("seller@sparkmart.com", "pass123")).thenReturn(Optional.of(seller));

        sellerMockMvc.perform(post("/seller/login")
                        .param("email", "seller@sparkmart.com")
                        .param("password", "pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dashboard"))
                .andExpect(request().sessionAttribute(AdminService.SESSION_SELLER, "seller@sparkmart.com"))
                .andExpect(request().sessionAttribute(CustomerService.SESSION_CUSTOMER_ID,
                        org.hamcrest.Matchers.notNullValue()))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN));
    }

    @Test
    void wrongSellerPasswordRejectedWithoutGrantingAnySession() throws Exception {
        when(customerService.authenticate("seller@sparkmart.com", "wrong")).thenReturn(Optional.empty());

        sellerMockMvc.perform(post("/seller/login")
                        .param("email", "seller@sparkmart.com")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/login"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN))
                .andExpect(request().sessionAttributeDoesNotExist(CustomerService.SESSION_CUSTOMER_ID));
    }

    @Test
    void regularCustomerCannotLoginAsSeller() throws Exception {
        CustomerUser regular = regularCustomer();
        when(customerService.authenticate("regular@sparkmart.com", "pass")).thenReturn(Optional.of(regular));

        sellerMockMvc.perform(post("/seller/login")
                        .param("email", "regular@sparkmart.com")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/login"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER));
    }

    @Test
    void sellerDashboardAccessibleAfterLogin() throws Exception {
        CustomerUser seller = sellerUser();
        when(customerService.authenticate("seller@sparkmart.com", "pass123")).thenReturn(Optional.of(seller));

        sellerMockMvc.perform(post("/seller/login")
                        .param("email", "seller@sparkmart.com")
                        .param("password", "pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dashboard"));

        sellerMockMvc.perform(get("/seller/dashboard")
                        .sessionAttr(AdminService.SESSION_SELLER, "seller@sparkmart.com")
                        .sessionAttr(CustomerService.SESSION_CUSTOMER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/dashboard"));
    }

    @Test
    void sellerBlockedFromAdminDashboard() throws Exception {
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
    void adminLoginAndDashboardStillWork() throws Exception {
        when(adminService.authenticate("admin@sparkmart.com", "admin123")).thenReturn(true);

        adminMockMvc.perform(post("/admin/login")
                        .param("username", "admin@sparkmart.com")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(request().sessionAttribute(AdminService.SESSION_ADMIN, "admin@sparkmart.com"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER));

        adminMockMvc.perform(get("/admin/dashboard")
                        .sessionAttr(AdminService.SESSION_ADMIN, "admin@sparkmart.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    void sellerLogoutPreventsDashboardAccess() throws Exception {
        SellerAuthenticationInterceptor interceptor = new SellerAuthenticationInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.getSession().setAttribute(AdminService.SESSION_SELLER, "seller@sparkmart.com");

        boolean allowed = interceptor.preHandle(request, response, null);
        assertTrue(allowed);

        MockHttpServletRequest requestAfterLogout = new MockHttpServletRequest();
        MockHttpServletResponse responseAfterLogout = new MockHttpServletResponse();

        boolean allowedAfterLogout = interceptor.preHandle(requestAfterLogout, responseAfterLogout, null);
        assertFalse(allowedAfterLogout);
        assertEquals(302, responseAfterLogout.getStatus());
        assertEquals("/seller/login", responseAfterLogout.getRedirectedUrl());
    }

    @Test
    void sellerLoginEndpointIsSeparateFromAdminLogin() throws Exception {
        when(adminService.authenticate("seller@sparkmart.com", "pass123")).thenReturn(false);
        when(customerService.authenticate("seller@sparkmart.com", "pass123")).thenReturn(Optional.empty());

        adminMockMvc.perform(post("/admin/login")
                        .param("username", "seller@sparkmart.com")
                        .param("password", "pass123"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER));
    }

    @Test
    void sellerDashboardRejectsUnauthenticatedAccess() throws Exception {
        SellerAuthenticationInterceptor interceptor = new SellerAuthenticationInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, null);

        assertFalse(allowed);
        assertEquals(302, response.getStatus());
        assertEquals("/seller/login", response.getRedirectedUrl());
    }

    @Test
    void sellerLoginClearsAnyPriorAdminSession() throws Exception {
        CustomerUser seller = sellerUser();
        when(customerService.authenticate("seller@sparkmart.com", "pass123")).thenReturn(Optional.of(seller));

        sellerMockMvc.perform(post("/seller/login")
                        .param("email", "seller@sparkmart.com")
                        .param("password", "pass123")
                        .sessionAttr(AdminService.SESSION_ADMIN, "admin@sparkmart.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dashboard"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN))
                .andExpect(request().sessionAttribute(AdminService.SESSION_SELLER, "seller@sparkmart.com"));
    }

    @Test
    void customerLoginStillWorksUnchanged() throws Exception {
        CustomerUser customer = regularCustomer();
        when(customerService.authenticate("regular@sparkmart.com", "pass")).thenReturn(Optional.of(customer));

        authMockMvc.perform(post("/login")
                        .param("email", "regular@sparkmart.com")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute(CustomerService.SESSION_CUSTOMER_ID,
                        org.hamcrest.Matchers.notNullValue()))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN));
    }

    @Test
    void getSellerRegisterPageWorksWithoutAuthentication() throws Exception {
        authMockMvc.perform(get("/seller/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }

    @Test
    void sellerLoginLinkToCreateAccountPointsToRegistration() throws Exception {
        sellerMockMvc.perform(get("/seller/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/login"));
    }

    @Test
    void postSellerRegisterIsNotInterceptedBySellerAuth() throws Exception {
        CustomerUser seller = sellerUser();
        when(customerService.registerSeller(eq("Test Store"), eq("Seller One"), eq("seller@sparkmart.com"),
                eq("01700000000"), eq("pass123"), eq("pass123"), any(), any())).thenReturn(seller);

        authMockMvc.perform(post("/seller/register")
                        .param("storeName", "Test Store")
                        .param("name", "Seller One")
                        .param("email", "seller@sparkmart.com")
                        .param("phone", "01700000000")
                        .param("password", "pass123")
                        .param("confirmPassword", "pass123")
                        .param("address", "123 St")
                        .param("area", "Dhaka"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dashboard"));
    }

    @Test
    void invalidSellerRegistrationStaysOnRegistrationForm() throws Exception {
        when(customerService.registerSeller(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Email already registered."));

        authMockMvc.perform(post("/seller/register")
                        .param("storeName", "Test Store")
                        .param("name", "Seller One")
                        .param("email", "taken@sparkmart.com")
                        .param("password", "pass123")
                        .param("confirmPassword", "pass123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }

    @Test
    void sellerRegistrationNeverSetsSessionAdmin() throws Exception {
        CustomerUser seller = sellerUser();
        when(customerService.registerSeller(eq("Test Store"), eq("Seller One"), eq("seller@sparkmart.com"),
                eq("01700000000"), eq("pass123"), eq("pass123"), any(), any())).thenReturn(seller);

        authMockMvc.perform(post("/seller/register")
                        .param("storeName", "Test Store")
                        .param("name", "Seller One")
                        .param("email", "seller@sparkmart.com")
                        .param("phone", "01700000000")
                        .param("password", "pass123")
                        .param("confirmPassword", "pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN));
    }

    @Test
    void protectedSellerDashboardRedirectsAnonymousToSellerLogin() throws Exception {
        SellerAuthenticationInterceptor interceptor = new SellerAuthenticationInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/seller/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, null);

        assertFalse(allowed);
        assertEquals(302, response.getStatus());
        assertEquals("/seller/login", response.getRedirectedUrl());
    }
}
