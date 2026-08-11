package com.example.spark_mart.customer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.spark_mart.order.OrderService;

class CustomerAuthControllerRoutingTest {

    private MockMvc mockMvc;
    private CustomerService customerService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        customerService = mock(CustomerService.class);
        orderService = mock(OrderService.class);
        CustomerAuthController controller = new CustomerAuthController(customerService, orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getLoginReturnsAuthViewWithLoginMode() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }

    @Test
    void getRegisterReturnsAuthViewWithRegisterMode() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }

    @Test
    void getLogoutRedirectsHomeAndClearsAdminSessions() throws Exception {
        mockMvc.perform(post("/logout")
                        .sessionAttr(com.example.spark_mart.admin.AdminService.SESSION_ADMIN, "admin@test.com")
                        .sessionAttr(com.example.spark_mart.admin.AdminService.SESSION_SELLER, "seller@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttributeDoesNotExist(com.example.spark_mart.admin.AdminService.SESSION_ADMIN))
                .andExpect(request().sessionAttributeDoesNotExist(com.example.spark_mart.admin.AdminService.SESSION_SELLER));
    }

    @Test
    void postLoginWithValidCustomerRedirectsHome() throws Exception {
        CustomerUser customer = new CustomerUser("John", "john@test.com", "01700000000", "hashed");
        when(customerService.authenticate("john@test.com", "pass123")).thenReturn(Optional.of(customer));

        mockMvc.perform(post("/login")
                        .param("email", "john@test.com")
                        .param("password", "pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void postLoginWithInvalidCredentialsReturnsAuthWithError() throws Exception {
        when(customerService.authenticate("wrong@test.com", "wrong")).thenReturn(Optional.empty());

        mockMvc.perform(post("/login")
                        .param("email", "wrong@test.com")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth"));
    }

    @Test
    void successfulSellerLoginSetsSellerSession() throws Exception {
        CustomerUser seller = new CustomerUser("Seller", "seller@test.com", "01700000000", "hashed");
        seller.becomeSeller("Test Store", "01700000000", "123 St", "Dhaka");
        when(customerService.authenticate("seller@test.com", "pass")).thenReturn(Optional.of(seller));

        mockMvc.perform(post("/login")
                        .param("email", "seller@test.com")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
