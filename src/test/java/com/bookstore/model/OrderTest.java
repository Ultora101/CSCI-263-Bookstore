package com.bookstore.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(1, "Ultora");
    }

    // Testing the constructor
    @Test
    void fullConstructor_shouldSetAllCorrectly() {
        assertEquals(1, order.getUserId());
        assertEquals("Ultora", order.getUsername());
        assertEquals(Order.Status.PENDING, order.getStatus());
        assertNotNull(order.getItems());
        assertTrue(order.getItems().isEmpty());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
    }

    // Null checker of Constructor
    @Test
    void noArgsToConstructor_shouldCreateUserWithNullFields() {
        Order emptyOrder = new Order();
        assertNotNull(emptyOrder.getItems());
        assertTrue(emptyOrder.getItems().isEmpty());
    }

    // Testing Setters and Getters
    @Test
    void setId_shouldUpdateId() {
        order.setId(20);
        assertEquals(20, order.getId());
    }

    @Test
    void setUserId_shouldUpdateUserId() {
        order.setUserId(57);
        assertEquals(57, order.getUserId());
    }

    @Test
    void setUsername_shouldUpdateUsername() {
        order.setUsername("newUser");
        assertEquals("newUser", order.getUsername());
    }

    @Test
    void setItems_shouldUpdateItems() {
        List<OrderItem> items = new ArrayList<>();
        order.setItems(items);
        assertEquals(items, order.getItems());
    }

    @Test
    void setSubtotal_shouldUpdateSubtotal() {
        BigDecimal subtotal = new BigDecimal("29.99");
        order.setSubtotal(subtotal);
        assertEquals(subtotal, order.getSubtotal());
    }

    @Test
    void setDiscountAmount_shouldUpdateDiscountAmount() {
        BigDecimal discount = new BigDecimal("5.00");
        order.setDiscountAmount(discount);
        assertEquals(discount, order.getDiscountAmount());
    }

    @Test
    void setTaxAmount_shouldUpdateTaxAmount() {
        BigDecimal tax = new BigDecimal("2.50");
        order.setTaxAmount(tax);
        assertEquals(tax, order.getTaxAmount());
    }

    @Test
    void setTotalAmount_shouldUpdateTotalAmount() {
        BigDecimal total = new BigDecimal("27.49");
        order.setTotalAmount(total);
        assertEquals(total, order.getTotalAmount());
    }

    @Test
    void setStatus_shouldUpdateStatus() {
        order.setStatus(Order.Status.SHIPPED);
        assertEquals(Order.Status.SHIPPED, order.getStatus());
    }

    @Test
    void setShippingAddress_shouldUpdateShippingAddress() {
        order.setShippingAddress("123 New Address, town, state 12345");
        assertEquals("123 New Address, town, state 12345", order.getShippingAddress());
    }

    @Test
    void setCreatedAt_shouldUpdateCreatedAt() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 15, 10, 30);
        order.setCreatedAt(timestamp);
        assertEquals(timestamp, order.getCreatedAt());
    }

    @Test
    void setUpdatedAt_shouldUpdateUpdatedAt() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 15, 10, 30);
        order.setUpdatedAt(timestamp);
        assertEquals(timestamp, order.getUpdatedAt());
    }

    // Edge case testing
    @Test
    void setItems_shouldAllowNull() {
        assertDoesNotThrow(() -> order.setItems(null));
        assertNull(order.getItems());
    }

    @Test
    void setTotalAmount_shouldHandleZeroValue() {
        order.setTotalAmount(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
    }

    @Test
    void setTotalAmount_shouldHandleLargeValue() {
        BigDecimal largeValue = new BigDecimal("999999.99");
        order.setTotalAmount(largeValue);
        assertEquals(largeValue, order.getTotalAmount());
    }

    // Testing Status Enum
    @Test
    void status_enumsShouldHaveExactlySixValues() {
        assertEquals(6, Order.Status.values().length);
    }

    @Test
    void status_shouldTransitionFromPendingToConfirmed() {
        assertEquals(Order.Status.PENDING, order.getStatus());
        order.setStatus(Order.Status.CONFIRMED);
        assertEquals(Order.Status.CONFIRMED, order.getStatus());
    }

    @Test
    void status_shouldTransitionFromPendingToCancelled() {
        assertEquals(Order.Status.PENDING, order.getStatus());
        order.setStatus(Order.Status.CANCELLED);
        assertEquals(Order.Status.CANCELLED, order.getStatus());
    }

    // testing the toString method
    @Test
    void toString_shouldContainIdAndUserId() {
        order.setId(18);
        String result = order.toString();
        assertTrue(result.contains("18"));
        assertTrue(result.contains("1"));
    }

    @Test
    void toString_shouldContainTotalAmountAndStatus() {
        order.setTotalAmount(new BigDecimal("50.99"));
        String result = order.toString();
        assertTrue(result.contains("50.99"));
        assertTrue(result.contains("PENDING"));
    }

}
