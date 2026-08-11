package com.example.spark_mart.seller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;

import com.example.spark_mart.admin.AdminService;
import com.example.spark_mart.catalog.CatalogService;
import com.example.spark_mart.customer.CustomerService;
import com.example.spark_mart.customer.CustomerUser;
import com.example.spark_mart.order.OrderService;
import com.example.spark_mart.security.SellerAuthenticationInterceptor;

class SellerControllerRoutingTest {

    private MockMvc mockMvc;
    private CatalogService catalogService;
    private OrderService orderService;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        catalogService = mock(CatalogService.class);
        orderService = mock(OrderService.class);
        customerService = mock(CustomerService.class);

        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.http.HttpSession session = invocation.getArgument(0);
            session.setAttribute(CustomerService.SESSION_CUSTOMER_ID, 1L);
            return null;
        }).when(customerService).login(any(), any());

        SellerController controller = new SellerController(catalogService, orderService, customerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private CustomerUser sellerUser() {
        CustomerUser user = new CustomerUser("Seller One", "seller@sparkmart.com", "01700000000", "hashedpw");
        user.becomeSeller("Test Store", "01700000000", "123 St", "Dhaka");
        return user;
    }

    @Test
    void getLoginReturnsSellerLoginView() throws Exception {
        mockMvc.perform(get("/seller/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/login"));
    }

    @Test
    void postSellerLoginWithValidCredentialsSetsSessionSellerAndRedirectsToDashboard() throws Exception {
        CustomerUser seller = sellerUser();
        when(customerService.authenticate("seller@sparkmart.com", "pass123")).thenReturn(Optional.of(seller));

        mockMvc.perform(post("/seller/login")
                        .param("email", "seller@sparkmart.com")
                        .param("password", "pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dashboard"))
                .andExpect(request().sessionAttribute(AdminService.SESSION_SELLER, "seller@sparkmart.com"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN));

        verify(customerService).login(any(), org.mockito.ArgumentMatchers.eq(seller));
    }

    @Test
    void postSellerLoginWithWrongPasswordReturnsError() throws Exception {
        when(customerService.authenticate("seller@sparkmart.com", "wrong")).thenReturn(Optional.empty());

        mockMvc.perform(post("/seller/login")
                        .param("email", "seller@sparkmart.com")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/login"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN));
    }

    @Test
    void postSellerLoginWithNonSellerAccountReturnsError() throws Exception {
        CustomerUser regularCustomer = new CustomerUser("Regular", "regular@test.com", "01700000000", "hashedpw");
        when(customerService.authenticate("regular@test.com", "pass")).thenReturn(Optional.of(regularCustomer));

        mockMvc.perform(post("/seller/login")
                        .param("email", "regular@test.com")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/login"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER));
    }

    @Test
    void postSellerLoginNeverSetsSessionAdmin() throws Exception {
        CustomerUser seller = sellerUser();
        when(customerService.authenticate("seller@sparkmart.com", "pass123")).thenReturn(Optional.of(seller));

        mockMvc.perform(post("/seller/login")
                        .param("email", "seller@sparkmart.com")
                        .param("password", "pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_ADMIN));
    }

    @Test
    void sellerLoginSetsSessionCustomerId() throws Exception {
        CustomerUser seller = sellerUser();
        when(customerService.authenticate("seller@sparkmart.com", "pass123")).thenReturn(Optional.of(seller));

        mockMvc.perform(post("/seller/login")
                        .param("email", "seller@sparkmart.com")
                        .param("password", "pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(request().sessionAttribute(CustomerService.SESSION_CUSTOMER_ID,
                        org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void sellerLogoutRedirectsHomeAndClearsSellerSession() throws Exception {
        mockMvc.perform(post("/seller/logout")
                        .sessionAttr(AdminService.SESSION_SELLER, "seller@test.com")
                        .sessionAttr(CustomerService.SESSION_CUSTOMER_ID, 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttributeDoesNotExist(AdminService.SESSION_SELLER))
                .andExpect(request().sessionAttributeDoesNotExist(CustomerService.SESSION_CUSTOMER_ID));
    }

    @Test
    void getDashboardReturnsSellerDashboardView() throws Exception {
        mockMvc.perform(get("/seller/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/dashboard"));
    }

    @Test
    void getProductsReturnsSellerProductsView() throws Exception {
        mockMvc.perform(get("/seller/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/products"));
    }

    @Test
    void getOrdersReturnsSellerOrdersView() throws Exception {
        mockMvc.perform(get("/seller/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/orders"));
    }

    @Test
    void getCategoriesReturnsSellerCategoriesView() throws Exception {
        mockMvc.perform(get("/seller/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/categories"));
    }

    @Test
    void getInventoryReturnsSellerInventoryView() throws Exception {
        mockMvc.perform(get("/seller/inventory"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/inventory"));
    }

    @Test
    void getNewProductReturnsSellerProductFormView() throws Exception {
        mockMvc.perform(get("/seller/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/product-form"));
    }

    @Test
    void unauthenticatedSellerAccessRedirectsToSellerLogin() throws Exception {
        SellerAuthenticationInterceptor interceptor = new SellerAuthenticationInterceptor();
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, null);

        assertFalse(allowed);
        assertEquals(302, response.getStatus());
        assertEquals("/seller/login", response.getRedirectedUrl());
    }

    @Test
    void authenticatedSellerAccessAllowed() throws Exception {
        SellerAuthenticationInterceptor interceptor = new SellerAuthenticationInterceptor();
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();
        request.getSession().setAttribute(AdminService.SESSION_SELLER, "seller@sparkmart.com");

        boolean allowed = interceptor.preHandle(request, response, null);

        assertTrue(allowed);
    }
}
