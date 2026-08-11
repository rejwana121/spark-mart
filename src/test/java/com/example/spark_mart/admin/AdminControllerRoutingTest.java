package com.example.spark_mart.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.spark_mart.catalog.CatalogService;
import com.example.spark_mart.customer.CustomerService;
import com.example.spark_mart.order.OrderService;

class AdminControllerRoutingTest {

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
    void getLoginPageReturnsAdminLoginView() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));
    }

    @Test
    void postLoginWithValidCredentialsRedirectsToDashboard() throws Exception {
        when(adminService.authenticate("admin@sparkmart.com", "admin123")).thenReturn(true);

        mockMvc.perform(post("/admin/login")
                        .param("username", "admin@sparkmart.com")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void postLoginWithInvalidCredentialsReturnsLoginWithError() throws Exception {
        when(adminService.authenticate("wrong", "wrong")).thenReturn(false);

        mockMvc.perform(post("/admin/login")
                        .param("username", "wrong")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));
    }

    @Test
    void adminLogoutClearsAllSessions() throws Exception {
        mockMvc.perform(post("/admin/logout")
                        .sessionAttr(AdminService.SESSION_ADMIN, "admin@sparkmart.com")
                        .sessionAttr(AdminService.SESSION_SELLER, "seller@test.com")
                        .sessionAttr(CustomerService.SESSION_CUSTOMER_ID, 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER))
                .andExpect(request().sessionAttributeDoesNotExist(CustomerService.SESSION_CUSTOMER_ID));
    }

    @Test
    void getDashboardReturnsDashboardView() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    void getProductsReturnsProductsView() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/products"));
    }

    @Test
    void getOrdersReturnsOrdersView() throws Exception {
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders"));
    }

    @Test
    void getCategoriesReturnsCategoriesView() throws Exception {
        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories"));
    }

    @Test
    void getCustomersReturnsCustomersView() throws Exception {
        mockMvc.perform(get("/admin/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customers"));
    }

    @Test
    void getPaymentsReturnsPaymentsView() throws Exception {
        mockMvc.perform(get("/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/payments"));
    }
}
