package com.bookstore.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class CartItemTest {

    private CartItem cartItem;
    private BigDecimal price = new BigDecimal("19.99");

    @BeforeEach
    void setUp() {
        cartItem = new CartItem(1, "Test Title", "Author Name", price, 5);
    }

    // Testing full constructor
    @Test
    void fullConstructor_shouldSetAllCorrectly() {
        assertEquals(1, cartItem.getBookId());
        assertEquals("Test Title", cartItem.getTitle());
        assertEquals("Author Name", cartItem.getAuthor());
        assertEquals(new BigDecimal("19.99"), cartItem.getPrice());
        assertEquals(5, cartItem.getQuantity());
    }

    // Testing Null Constructor
    @Test
    void noArgsToConstructor_shouldCreateUserWithNullFields() {
        CartItem emptyCartItem = new CartItem();
        assertEquals(0, emptyCartItem.getBookId());
        assertNull(emptyCartItem.getTitle());
        assertNull(emptyCartItem.getAuthor());
        assertNull(emptyCartItem.getPrice());
        assertEquals(0, emptyCartItem.getQuantity());
    }

    // Testing Getters and Setters
    @Test
    void setBookId_shouldUpdateBookId() {
        cartItem.setBookId(40);
        assertEquals(40, cartItem.getBookId());
    }

    @Test
    void setTitle_shouldUpdateTitle() {
        cartItem.setTitle("New Title");
        assertEquals("New Title", cartItem.getTitle());
    }

    @Test
    void setAuthor_shouldUpdateAuthor() {
        cartItem.setAuthor("Funny Name");
        assertEquals("Funny Name", cartItem.getAuthor());
    }

    @Test
    void setPrice_shouldUpdatePrice() {
        cartItem.setPrice(new BigDecimal("50.99"));
        assertEquals(new BigDecimal("50.99"), cartItem.getPrice());
    }

    @Test
    void setQuantity_shouldUpdateQuantity() {
        cartItem.setQuantity(2);
        assertEquals(2, cartItem.getQuantity());
    }

    // Testing getSubtotal
    @Test
    void getSubtotal_shouldReturnCorrectValue_whenPriceAndQuantityAreNormal() {
        cartItem.setPrice(new BigDecimal("19.99"));
        cartItem.setQuantity(3);
        BigDecimal result = cartItem.getSubtotal();
        assertEquals(0, result.compareTo(new BigDecimal("59.97")));
    }

    @Test
    void getSubtotal_shouldReturnPrice_whenQuantityIsOne() {
        cartItem.setPrice(new BigDecimal("19.99"));
        cartItem.setQuantity(1);
        BigDecimal result = cartItem.getSubtotal();
        assertEquals(0, result.compareTo(new BigDecimal("19.99")));
    }

    @Test
    void getSubtotal_shouldReturnZero_whenQuantityIsZero() {
        cartItem.setPrice(new BigDecimal("19.99"));
        cartItem.setQuantity(0);
        BigDecimal result = cartItem.getSubtotal();
        assertEquals(0, result.compareTo(new BigDecimal("0")));
    }

    @Test
    void getSubtotal_shouldThrowException_whenPriceIsNull() {
        cartItem.setPrice(null);
        assertThrows(NullPointerException.class, () -> cartItem.getSubtotal());
    }

    // Testing toString method
    @Test
    void toString_shouldContainTitle() {
        String result = cartItem.toString();
        assertTrue(result.contains("Test Title"));
    }

    @Test
    void tostring_shouldContainQuantity() {
        String result = cartItem.toString();
        assertTrue(result.contains("5"));
    }

    @Test
    void toString_shouldContainSubtotal() {
        String result = cartItem.toString();
        assertTrue(result.contains("99.95")); // 19.99 x 5
    }
}
