package com.bookstore.service;

import com.bookstore.dao.BookDAO;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookDAO mockBookDAO;

    private BookService bookService;
    private Book testBook;

    @BeforeEach
    void setUp() throws Exception {
        bookService = new BookService();

        Field field = BookService.class.getDeclaredField("bookDAO");
        field.setAccessible(true);
        field.set(bookService, mockBookDAO);

        testBook = new Book(1, "Test Book", "Test Author", "Fiction", "1234567890123",
                new BigDecimal("19.99"), 10, "A test description.", "Publisher Inc.", 2020);
    }

    // getAllBooks
    @Test
    void getAllBooks_shouldReturnAllBooksFromDAO() {
        when(mockBookDAO.findAll()).thenReturn(List.of(testBook));
        List<Book> result = bookService.getAllBooks();
        assertEquals(1, result.size());
        verify(mockBookDAO).findAll();
    }

    // getBookById

    @Test
    void getBookById_shouldReturnBook_whenFound() {
        when(mockBookDAO.findById(1)).thenReturn(Optional.of(testBook));
        Book result = bookService.getBookById(1);
        assertEquals("Test Book", result.getTitle());
    }

    @Test
    void getBookById_shouldThrowBookNotFoundException_whenNotFound() {
        when(mockBookDAO.findById(999)).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> bookService.getBookById(999));
    }

    // searchBooks
    @Test
    void searchBooks_shouldReturnMatchingBooks() {
        when(mockBookDAO.search("test")).thenReturn(List.of(testBook));
        List<Book> result = bookService.searchBooks("test");
        assertEquals(1, result.size());
    }

    @Test
    void searchBooks_shouldReturnAllBooks_whenQueryIsNull() {
        when(mockBookDAO.findAll()).thenReturn(List.of(testBook));
        List<Book> result = bookService.searchBooks(null);
        verify(mockBookDAO).findAll();
        assertEquals(1, result.size());
    }

    @Test
    void searchBooks_shouldReturnAllBooks_whenQueryIsBlank() {
        when(mockBookDAO.findAll()).thenReturn(List.of(testBook));
        List<Book> result = bookService.searchBooks("   ");
        verify(mockBookDAO).findAll();
        assertEquals(1, result.size());
    }

    // searchByField
    @Test
    void searchByField_shouldDelegateToDAO() {
        when(mockBookDAO.searchByField("title", "test")).thenReturn(List.of(testBook));
        List<Book> result = bookService.searchByField("title", "test");
        assertEquals(1, result.size());
    }

    @Test
    void searchByField_shouldReturnAllBooks_whenQueryIsBlank() {
        when(mockBookDAO.findAll()).thenReturn(List.of(testBook));
        bookService.searchByField("title", "");
        verify(mockBookDAO).findAll();
    }

    // getAllGenres
    @Test
    void getAllGenres_shouldReturnGenresFromDAO() {
        when(mockBookDAO.findAllGenres()).thenReturn(List.of("Fiction", "Non-Fiction"));
        List<String> genres = bookService.getAllGenres();
        assertEquals(2, genres.size());
        assertTrue(genres.contains("Fiction"));
    }

    // addBook
    @Test
    void addBook_shouldCreateBook_whenValid() {
        when(mockBookDAO.create(testBook)).thenReturn(testBook);
        Book result = bookService.addBook(testBook);
        assertEquals("Test Book", result.getTitle());
        verify(mockBookDAO).create(testBook);
    }

    @Test
    void addBook_shouldThrow_whenTitleIsBlank() {
        testBook.setTitle("  ");
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(testBook));
    }

    @Test
    void addBook_shouldThrow_whenTitleIsNull() {
        testBook.setTitle(null);
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(testBook));
    }

    @Test
    void addBook_shouldThrow_whenAuthorIsBlank() {
        testBook.setAuthor("");
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(testBook));
    }

    @Test
    void addBook_shouldThrow_whenGenreIsNull() {
        testBook.setGenre(null);
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(testBook));
    }

    @Test
    void addBook_shouldThrow_whenPriceIsZero() {
        testBook.setPrice(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(testBook));
    }

    @Test
    void addBook_shouldThrow_whenPriceIsNegative() {
        testBook.setPrice(new BigDecimal("-5.00"));
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(testBook));
    }

    @Test
    void addBook_shouldThrow_whenStockIsNegative() {
        testBook.setStockQuantity(-1);
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(testBook));
    }

    // updateBook
    @Test
    void updateBook_shouldCallUpdate_whenBookExists() {
        when(mockBookDAO.findById(1)).thenReturn(Optional.of(testBook));
        doNothing().when(mockBookDAO).update(testBook);
        assertDoesNotThrow(() -> bookService.updateBook(testBook));
        verify(mockBookDAO).update(testBook);
    }

    @Test
    void updateBook_shouldThrowBookNotFoundException_whenBookDoesNotExist() {
        when(mockBookDAO.findById(1)).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> bookService.updateBook(testBook));
    }

    @Test
    void updateBook_shouldThrow_whenValidationFails() {
        testBook.setTitle(null);
        assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(testBook));
    }

    // deleteBook
    @Test
    void deleteBook_shouldCallDelete_whenBookExists() {
        when(mockBookDAO.findById(1)).thenReturn(Optional.of(testBook));
        doNothing().when(mockBookDAO).delete(1);
        assertDoesNotThrow(() -> bookService.deleteBook(1));
        verify(mockBookDAO).delete(1);
    }

    @Test
    void deleteBook_shouldThrowBookNotFoundException_whenBookDoesNotExist() {
        when(mockBookDAO.findById(999)).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(999));
    }

    // updateStock
    @Test
    void updateStock_shouldCallDAOUpdateStock_whenQuantityIsValid() {
        doNothing().when(mockBookDAO).updateStock(1, 20);
        assertDoesNotThrow(() -> bookService.updateStock(1, 20));
        verify(mockBookDAO).updateStock(1, 20);
    }

    @Test
    void updateStock_shouldAllowZero() {
        doNothing().when(mockBookDAO).updateStock(1, 0);
        assertDoesNotThrow(() -> bookService.updateStock(1, 0));
    }

    @Test
    void updateStock_shouldThrow_whenQuantityIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> bookService.updateStock(1, -1));
    }
}