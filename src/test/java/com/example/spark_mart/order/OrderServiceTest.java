package com.example.spark_mart.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.spark_mart.catalog.CatalogService;

/**
 * Unit tests for OrderService's fulfillment-status pipeline
 * (Chapter 6.2: fast, isolated tests using Mockito — no Spring context,
 * no real database).
 */
class OrderServiceTest {

    private OrderRepository orderRepository;
    private PaymentRepository paymentRepository;
    private CatalogService catalogService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        catalogService = mock(CatalogService.class);
        orderService = new OrderService(orderRepository, paymentRepository, catalogService);
    }

    private OrderRecord orderWithStatus(String orderNumber, String fulfillmentStatus) {
        OrderRecord order = new OrderRecord(orderNumber, 1L, "Test Customer", "test@example.com", "01700000000",
                "Test Address", "Test Area", null, "Cash on Delivery", java.util.List.of(),
                java.math.BigDecimal.ZERO, java.math.BigDecimal.valueOf(80), java.math.BigDecimal.valueOf(80));
        order.setFulfillmentStatus(fulfillmentStatus);
        return order;
    }

    @Test
    void allowsTheNextStepInTheApprovedSequence() {
        OrderRecord order = orderWithStatus("SM-ORD-000001", "Order Sent");
        when(orderRepository.findById(eq("SM-ORD-000001"))).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateStatus("SM-ORD-000001", "Approved", null);

        assertEquals("Approved", order.getFulfillmentStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void rejectsSkippingStraightToDelivered() {
        OrderRecord order = orderWithStatus("SM-ORD-000002", "Order Sent");
        when(orderRepository.findById(eq("SM-ORD-000002"))).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateStatus("SM-ORD-000002", "Delivered", null);

        assertEquals("Order Sent", order.getFulfillmentStatus());
    }

    @Test
    void allowsCancellingFromOrderSent() {
        OrderRecord order = orderWithStatus("SM-ORD-000003", "Order Sent");
        when(orderRepository.findById(eq("SM-ORD-000003"))).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateStatus("SM-ORD-000003", "Cancelled", null);

        assertEquals("Cancelled", order.getFulfillmentStatus());
    }

    @Test
    void rejectsProcessingBeforeApproval() {
        OrderRecord order = orderWithStatus("SM-ORD-000004", "Order Sent");
        when(orderRepository.findById(eq("SM-ORD-000004"))).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateStatus("SM-ORD-000004", "Processing", null);

        assertEquals("Order Sent", order.getFulfillmentStatus());
    }
}
