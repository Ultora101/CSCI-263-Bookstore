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
}
