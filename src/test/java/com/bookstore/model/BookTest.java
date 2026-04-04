package com.bookstore.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    private Book book;
    private BigDecimal price = new BigDecimal("19.99");

    @BeforeEach
    void setUp() {
        book = new Book(1, "A Real Book Title", "Author Name", "Testing Genre", "0123456789987", price, 5,
                "This is a test description.", "Publish Inc.", 2008);
    }

    // Testing the Constructor
    @Test
    void fullConstructor_shouldSetAllCorrectly() {
        assertEquals(1, book.getId());
        assertEquals("A Real Book Title", book.getTitle());
        assertEquals("Author Name", book.getAuthor());
        assertEquals("Testing Genre", book.getGenre());
        assertEquals("0123456789987", book.getIsbn());
        assertEquals(new BigDecimal("19.99"), book.getPrice());
        assertEquals(5, book.getStockQuantity());
        assertEquals("This is a test description.", book.getDescription());
        assertEquals("Publish Inc.", book.getPublisher());
        assertEquals(2008, book.getPublishYear());
        assertNull(book.getCoverImagePath());
    }

    // Testing the Null of Constructor
    @Test
    void noArgsToConstructor_shouldCreateUserWithNullFields() {
        Book emptyBook = new Book();
        assertEquals(0, emptyBook.getId());
        assertNull(emptyBook.getTitle());
        assertNull(emptyBook.getAuthor());
        assertNull(emptyBook.getGenre());
        assertNull(emptyBook.getIsbn());
        assertNull(emptyBook.getPrice());
        assertEquals(0, emptyBook.getStockQuantity());
        assertNull(emptyBook.getDescription());
        assertNull(emptyBook.getPublisher());
        assertEquals(0, emptyBook.getPublishYear());
    }

    // Testing the Setters
    @Test
    void setId_shouldUpdateId() {
        book.setId(14);
        assertEquals(14, book.getId());
    }

    @Test
    void setTitle_shouldUpdateTitle() {
        book.setTitle("New Title");
        assertEquals("New Title", book.getTitle());
    }

    @Test
    void setAuthor_shouldUpdateAuthor() {
        book.setAuthor("Cool Name");
        assertEquals("Cool Name", book.getAuthor());
    }

    @Test
    void setGenre_shouldUpdateGenre() {
        book.setGenre("Non-Fiction");
        assertEquals("Non-Fiction", book.getGenre());
    }

    @Test
    void setIsbn_shouldUpdateIsbn() {
        book.setIsbn("7899876543210");
        assertEquals("7899876543210", book.getIsbn());
    }

    @Test
    void setPrice_shouldUpdatePrice() {
        book.setPrice(new BigDecimal(15.00));
        assertEquals(new BigDecimal(15.00), book.getPrice());
    }

    @Test
    void setStockQuantity_shouldUpdateStockQuantity() {
        book.setStockQuantity(50);
        assertEquals(50, book.getStockQuantity());
    }

    @Test
    void setDescription_shouldUpdateDescription() {
        book.setDescription("New Description");
        assertEquals("New Description", book.getDescription());
    }

    @Test
    void setPublisher_shouldUpdatePublisher() {
        book.setPublisher("New Publisher");
        assertEquals("New Publisher", book.getPublisher());
    }

    @Test
    void setPublishYear_shouldUpdatePublishYear() {
        book.setPublishYear(1999);
        assertEquals(1999, book.getPublishYear());
    }

    @Test
    void setCoverImagePath_shouldUpdatePath() {
        book.setCoverImagePath("/new/image/path");
        assertEquals("/new/image/path", book.getCoverImagePath());
    }

    // testing isAvalable
    @Test
    void isAvalable_shouldReturnTrue_whenStockIsGreaterThanZero() {
        book.setStockQuantity(5);
        assertTrue(book.isAvailable());
    }

    @Test
    void isAvailable_shouldReturnFalse_whenStockIsZero() {
        book.setStockQuantity(0);
        assertFalse(book.isAvailable());
    }

    @Test
    void isAvailable_shouldReturnFalse_whenStockIsNegative() {
        book.setStockQuantity(-1);
        assertFalse(book.isAvailable());
    }

    // testing toString
    @Test
    void toString_shouldContainTitleAndAuthor() {
        String result = book.toString();
        assertTrue(result.contains("A Real Book Title"));
        assertTrue(result.contains("Author Name"));
    }

    @Test
    void toString_shouldContainPriceAndStockQuantity() {
        String result = book.toString();
        assertTrue(result.contains("19.99"));
        assertTrue(result.contains("5"));
    }

}
