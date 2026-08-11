package com.example.spark_mart.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

import com.example.spark_mart.admin.AdminService;
import com.example.spark_mart.order.OrderService;

class CustomerAuthControllerLoginTest {

    private CustomerService customerService;
    private OrderService orderService;
    private CustomerAuthController controller;

    @BeforeEach
    void setUp() {
        customerService = mock(CustomerService.class);
        orderService = mock(OrderService.class);
        controller = new CustomerAuthController(customerService, orderService);
    }

    private CustomerUser sellerUser() {
        CustomerUser user = new CustomerUser("Seller One", "seller@sparkmart.com", "01700000000", "hashedpw");
        user.becomeSeller("Test Store", "01700000000", "123 St", "Dhaka");
        return user;
    }

    private CustomerUser regularCustomer() {
        return new CustomerUser("Customer One", "customer@sparkmart.com", "01800000000", "hashedpw");
    }

    @Test
    void existingSellerNormalLoginSetsSessionCustomerIdAndSessionSellerButNotSessionAdmin() {
        CustomerUser seller = sellerUser();
        when(customerService.authenticate("seller@sparkmart.com", "pass"))
                .thenReturn(Optional.of(seller));

        MockHttpSession session = new MockHttpSession();
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.doLogin("seller@sparkmart.com", "pass", session, model);

        assertEquals("redirect:/", view);
        assertEquals(seller.getId(), session.getAttribute(CustomerService.SESSION_CUSTOMER_ID));
        assertEquals("seller@sparkmart.com", session.getAttribute(AdminService.SESSION_SELLER));
        assertNull(session.getAttribute(AdminService.SESSION_ADMIN));
    }

    @Test
    void regularCustomerLoginSetsSessionCustomerIdButNeitherSessionSellerNorSessionAdmin() {
        CustomerUser customer = regularCustomer();
        when(customerService.authenticate("customer@sparkmart.com", "pass"))
                .thenReturn(Optional.of(customer));

        MockHttpSession session = new MockHttpSession();
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.doLogin("customer@sparkmart.com", "pass", session, model);

        assertEquals("redirect:/", view);
        assertEquals(customer.getId(), session.getAttribute(CustomerService.SESSION_CUSTOMER_ID));
        assertNull(session.getAttribute(AdminService.SESSION_SELLER));
        assertNull(session.getAttribute(AdminService.SESSION_ADMIN));
    }

    @Test
    void failedLoginDoesNotGrantAnyRoleSession() {
        when(customerService.authenticate("unknown@sparkmart.com", "wrong"))
                .thenReturn(Optional.empty());

        MockHttpSession session = new MockHttpSession();
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.doLogin("unknown@sparkmart.com", "wrong", session, model);

        assertEquals("auth", view);
        assertNull(session.getAttribute(CustomerService.SESSION_CUSTOMER_ID));
        assertNull(session.getAttribute(AdminService.SESSION_SELLER));
        assertNull(session.getAttribute(AdminService.SESSION_ADMIN));
        assertEquals("Invalid email or password.", model.getAttribute("error"));
    }
}
