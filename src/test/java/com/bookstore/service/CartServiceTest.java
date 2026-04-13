package com.bookstore.service;

import com.bookstore.exception.InsufficientStockException;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CartServiceTest {

    private CartService cartService;
    private Book testBook;

    @BeforeEach
    void setUp() {
        cartService = new CartService();
        testBook = new Book(1, "Test Book", "Test Author", "Fiction", "1234567890123",
                new BigDecimal("20.00"), 10, "A test book.", "Publisher", 2020);
    }

    // addToCart
    @Test
    void addToCart_shouldAddItemToCart() {
        cartService.addToCart(testBook, 2);
        assertEquals(1, cartService.getCartItems().size());
        assertEquals(2, cartService.getCartItems().get(0).getQuantity());
    }

    @Test
    void addToCart_shouldIncreaseQuantity_whenBookAlreadyInCart() {
        cartService.addToCart(testBook, 2);
        cartService.addToCart(testBook, 3);
        assertEquals(5, cartService.getCartItems().get(0).getQuantity());
    }

    @Test
    void addToCart_shouldThrow_whenQuantityIsZero() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(testBook, 0));
    }

    @Test
    void addToCart_shouldThrow_whenQuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(testBook, -1));
    }

    @Test
    void addToCart_shouldThrow_whenQuantityExceedsStock() {
        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(testBook, 11));
    }

    @Test
    void addToCart_shouldThrow_whenCombinedQuantityExceedsStock() {
        cartService.addToCart(testBook, 8);
        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(testBook, 5));
    }

    // removeFromCart
    @Test
    void removeFromCart_shouldRemoveItem_whenItemExists() {
        cartService.addToCart(testBook, 1);
        cartService.removeFromCart(testBook.getId());
        assertTrue(cartService.isEmpty());
    }

    @Test
    void removeFromCart_shouldDoNothing_whenItemDoesNotExist() {
        assertDoesNotThrow(() -> cartService.removeFromCart(999));
        assertTrue(cartService.isEmpty());
    }

    // updateQuantity
    @Test
    void updateQuantity_shouldUpdateItemQuantity() {
        cartService.addToCart(testBook, 2);
        cartService.updateQuantity(testBook.getId(), 5);
        assertEquals(5, cartService.getCartItems().get(0).getQuantity());
    }

    @Test
    void updateQuantity_shouldRemoveItem_whenQuantityIsZero() {
        cartService.addToCart(testBook, 2);
        cartService.updateQuantity(testBook.getId(), 0);
        assertTrue(cartService.isEmpty());
    }

    @Test
    void updateQuantity_shouldRemoveItem_whenQuantityIsNegative() {
        cartService.addToCart(testBook, 2);
        cartService.updateQuantity(testBook.getId(), -1);
        assertTrue(cartService.isEmpty());
    }

    // clearCart
    @Test
    void clearCart_shouldRemoveAllItems() {
        cartService.addToCart(testBook, 2);
        Book secondBook = new Book(2, "Second Book", "Author Two", "Non-Fiction", "9876543210123",
                new BigDecimal("15.00"), 5, "Another book.", "Publisher", 2021);
        cartService.addToCart(secondBook, 1);
        cartService.clearCart();
        assertTrue(cartService.isEmpty());
    }

    // getItemCount
    @Test
    void getItemCount_shouldReturnTotalQuantityAcrossAllItems() {
        Book secondBook = new Book(2, "Second Book", "Author Two", "Non-Fiction", "9876543210123",
                new BigDecimal("15.00"), 5, "Another book.", "Publisher", 2021);
        cartService.addToCart(testBook, 3);
        cartService.addToCart(secondBook, 2);
        assertEquals(5, cartService.getItemCount());
    }

    @Test
    void getItemCount_shouldReturnZero_whenCartIsEmpty() {
        assertEquals(0, cartService.getItemCount());
    }

    // isEmpty
    @Test
    void isEmpty_shouldReturnTrue_whenCartIsEmpty() {
        assertTrue(cartService.isEmpty());
    }

    @Test
    void isEmpty_shouldReturnFalse_afterAddingItem() {
        cartService.addToCart(testBook, 1);
        assertFalse(cartService.isEmpty());
    }

    // getSubtotal
    @Test
    void getSubtotal_shouldReturnCorrectSum() {
        // 2 x $20.00 = $40.00
        cartService.addToCart(testBook, 2);
        assertEquals(new BigDecimal("40.00"), cartService.getSubtotal());
    }

    @Test
    void getSubtotal_shouldReturnZero_whenCartIsEmpty() {
        assertEquals(BigDecimal.ZERO, cartService.getSubtotal());
    }

    // getDiscountAmount
    @Test
    void getDiscountAmount_shouldReturnZero_whenSubtotalIsBelowThreshold() {
        // 1 x $20.00 = $20.00, below $50 threshold
        cartService.addToCart(testBook, 1);
        assertEquals(BigDecimal.ZERO, cartService.getDiscountAmount());
    }

    @Test
    void getDiscountAmount_shouldReturn10Percent_whenSubtotalIsAtOrAboveThreshold() {
        // 3 x $20.00 = $60.00 -> 10% discount = $6.00
        cartService.addToCart(testBook, 3);
        assertEquals(new BigDecimal("6.00"), cartService.getDiscountAmount());
    }

    @Test
    void getDiscountAmount_shouldHandleExactThreshold() {
        // $50 exactly is AT the threshold, should trigger discount
        Book fiftyBook = new Book(3, "Fifty Book", "Author", "Fiction", "1111111111111",
                new BigDecimal("50.00"), 5, "desc", "Publisher", 2020);
        cartService.addToCart(fiftyBook, 1);
        assertEquals(new BigDecimal("5.00"), cartService.getDiscountAmount());
    }

    // getTaxAmount
    @Test
    void getTaxAmount_shouldApply8PercentAfterDiscount() {
        // 3 x $20.00 = $60.00 - $6.00 discount = $54.00 * 0.08 = $4.32
        cartService.addToCart(testBook, 3);
        assertEquals(new BigDecimal("4.32"), cartService.getTaxAmount());
    }

    @Test
    void getTaxAmount_shouldApplyOnFullSubtotal_whenNoDiscount() {
        // 1 x $20.00 = $20.00, no discount, tax = $1.60
        cartService.addToCart(testBook, 1);
        assertEquals(new BigDecimal("1.60"), cartService.getTaxAmount());
    }

    // getTotal
    @Test
    void getTotal_shouldReturnCorrectFinalAmount_withDiscount() {
        // 3 x $20.00 = $60.00 - $6.00 + $4.32 = $58.32
        cartService.addToCart(testBook, 3);
        assertEquals(new BigDecimal("58.32"), cartService.getTotal());
    }

    @Test
    void getTotal_shouldReturnCorrectFinalAmount_withoutDiscount() {
        // 1 x $20.00, no discount, + $1.60 tax = $21.60
        cartService.addToCart(testBook, 1);
        assertEquals(new BigDecimal("21.60"), cartService.getTotal());
    }

    // getCartItems
    @Test
    void getCartItems_shouldReturnAllItemsInCart() {
        cartService.addToCart(testBook, 2);
        List<CartItem> items = cartService.getCartItems();
        assertEquals(1, items.size());
        assertEquals(testBook.getId(), items.get(0).getBookId());
    }

    // rate / threshold getters
    @Test
    void getTaxRate_shouldReturn008() {
        assertEquals(0.08, cartService.getTaxRate());
    }

    @Test
    void getDiscountRate_shouldReturn010() {
        assertEquals(0.10, cartService.getDiscountRate());
    }

    @Test
    void getDiscountThreshold_shouldReturn50() {
        assertEquals(50.0, cartService.getDiscountThreshold());
    }
}