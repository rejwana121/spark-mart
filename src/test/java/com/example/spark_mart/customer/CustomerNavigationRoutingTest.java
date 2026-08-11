package com.example.spark_mart.customer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import com.example.spark_mart.address.AddressController;
import com.example.spark_mart.address.AddressService;
import com.example.spark_mart.order.OrderController;
import com.example.spark_mart.order.OrderService;

class CustomerNavigationRoutingTest {

    private MockMvc authMockMvc;
    private MockMvc orderMockMvc;
    private MockMvc addressMockMvc;

    @BeforeEach
    void setUp() {
        CustomerService customerService = mock(CustomerService.class);
        OrderService orderService = mock(OrderService.class);
        AddressService addressService = mock(AddressService.class);

        CustomerUser customer = new CustomerUser("Test", "test@test.com", "01700000000", "hashed");
        when(customerService.currentCustomer(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.of(customer));
        when(orderService.listForCustomer(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of());
        when(addressService.listForCustomer(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of());

        authMockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerAuthController(customerService, orderService))
                .setViewResolvers(createViewResolver())
                .build();
        orderMockMvc = MockMvcBuilders
                .standaloneSetup(new OrderController(orderService, customerService))
                .setViewResolvers(createViewResolver())
                .build();
        addressMockMvc = MockMvcBuilders
                .standaloneSetup(new AddressController(addressService, customerService))
                .setViewResolvers(createViewResolver())
                .build();
    }

    private ThymeleafViewResolver createViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(new SpringTemplateEngine());
        return resolver;
    }

    @Test
    void manageMyAccountMenuLinkRespondsOk() throws Exception {
        authMockMvc.perform(get("/profile"))
                .andExpect(status().isOk());
    }

    @Test
    void myOrdersMenuLinkRespondsOk() throws Exception {
        orderMockMvc.perform(get("/orders"))
                .andExpect(status().isOk());
    }

    @Test
    void myAddressesMenuLinkRespondsOk() throws Exception {
        addressMockMvc.perform(get("/addresses"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelAnOrderMenuLinkRespondsOk() throws Exception {
        orderMockMvc.perform(get("/orders"))
                .andExpect(status().isOk());
    }

    @Test
    void customerLoginMenuLinkRespondsOk() throws Exception {
        authMockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void createAccountMenuLinkRespondsOk() throws Exception {
        authMockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    void becomeASellerMenuLinkRespondsOk() throws Exception {
        authMockMvc.perform(get("/seller/register"))
                .andExpect(status().isOk());
    }

    @Test
    void noRouteExistsForRemovedMenuItems() throws Exception {
        authMockMvc.perform(get("/wishlist"))
                .andExpect(status().isNotFound());

        authMockMvc.perform(get("/reviews"))
                .andExpect(status().isNotFound());

        authMockMvc.perform(get("/returns"))
                .andExpect(status().isNotFound());
    }
}
