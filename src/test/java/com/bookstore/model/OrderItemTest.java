package com.bookstore.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class OrderItemTest {

    private OrderItem orderItem;
    private BigDecimal unitPrice = new BigDecimal("19.99");

    @BeforeEach
    void setUp() {
        orderItem = new OrderItem(1, "Test Title", "Author Name", 5, unitPrice);
    }

    // Testing the Constructor
    @Test
    void fullConstructor_shouldSetAllCorrectly() {
        assertEquals(0, orderItem.getId());
        assertEquals(0, orderItem.getOrderId());
        assertEquals(1, orderItem.getBookId());
        assertEquals("Test Title", orderItem.getBookTitle());
        assertEquals("Author Name", orderItem.getBookAuthor());
        assertEquals(5, orderItem.getQuantity());
        assertEquals(new BigDecimal("19.99"), orderItem.getUnitPrice());
    }

    @Test
    void noArgsToConstructor_shouldCreateUserWithNullFields() {
        OrderItem emptyOrderItem = new OrderItem();
        assertEquals(0, emptyOrderItem.getId());
        assertEquals(0, emptyOrderItem.getOrderId());
        assertEquals(0, emptyOrderItem.getBookId());
        assertNull(emptyOrderItem.getBookTitle());
        assertNull(emptyOrderItem.getBookAuthor());
        assertEquals(0, emptyOrderItem.getQuantity());
        assertNull(emptyOrderItem.getUnitPrice());
    }

    // Testing Setters and Getters
    @Test
    void setId_shouldUpdateId() {
        orderItem.setId(20);
        assertEquals(20, orderItem.getId());
    }

    @Test
    void setOrderId_shouldUpdateOrderId() {
        orderItem.setOrderId(30);
        assertEquals(30, orderItem.getOrderId());
    }

    @Test
    void setBookId_shouldUpdateBookId() {
        orderItem.setBookId(15);
        assertEquals(15, orderItem.getBookId());
    }

    @Test
    void setBookTitle_shouldUpdateBookTitle() {
        orderItem.setBookTitle("New Title");
        assertEquals("New Title", orderItem.getBookTitle());
    }

    @Test
    void setBookAuthor_shouldUpdateBookAuthor() {
        orderItem.setBookAuthor("New Author");
        assertEquals("New Author", orderItem.getBookAuthor());
    }

    @Test
    void setQuantity_shouldUpdateQuantity() {
        orderItem.setQuantity(500);
        assertEquals(500, orderItem.getQuantity());
    }

    @Test
    void setUnitPrice_shouldUpdateUnitPrice() {
        orderItem.setUnitPrice(new BigDecimal("10.99"));
        assertEquals(new BigDecimal("10.99"), orderItem.getUnitPrice());
    }

    // Testing the getSubtotal method
    @Test
    void getSubtotal_shouldReturnCorrectValue_whenUnitPriceAndQuantityAreNormal() {
        orderItem.setUnitPrice(new BigDecimal("5.99"));
        orderItem.setQuantity(3);
        BigDecimal result = orderItem.getSubtotal();
        assertEquals(0, result.compareTo(new BigDecimal("17.97")));
    }

    @Test
    void getSubtotal_shouldReturnUnitPrice_whenQuantityIsOne() {
        orderItem.setUnitPrice(new BigDecimal("10.99"));
        orderItem.setQuantity(1);
        BigDecimal result = orderItem.getSubtotal();
        assertEquals(0, result.compareTo(new BigDecimal("10.99")));
    }

    @Test
    void getSubtotal_shouldReturnZero_whenQuantityIsZero() {
        orderItem.setUnitPrice(new BigDecimal("10.99"));
        orderItem.setQuantity(0);
        BigDecimal result = orderItem.getSubtotal();
        assertEquals(0, result.compareTo(new BigDecimal("0")));
    }

    @Test
    void getSubtotal_shouldThrowException_whenUnitPriceIsNull() {
        orderItem.setUnitPrice(null);
        assertThrows(NullPointerException.class, () -> orderItem.getSubtotal());
    }
}
