package com.bookstore.service;

import com.bookstore.dao.BookDAO;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer for book operations
 * handles business logic for book management
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private final BookDAO bookDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
    }

    public List<Book> getAllBooks() {
        return bookDAO.findAll();
    }

    public Book getBookById(int id) {
        return bookDAO.findById(id).orElseThrow(() -> new BookNotFoundException("Book with id " + id + " not found"));
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.isBlank())
            return getAllBooks();
        return bookDAO.search(query.trim());
    }

    public List<Book> searchByField(String field, String query) {
        if (query == null || query.isBlank())
            return getAllBooks();
        return bookDAO.searchByField(field, query.trim());
    }

    public List<String> getAllGenres() {
        return bookDAO.findAllGenres();
    }

    /**
     * Adds a new book to the catalog (admin only)
     */
    public Book addBook(Book book) {
        validateBook(book);
        return bookDAO.create(book);
    }

    /**
     * Updates an exsisting book's details (admin only)
     */
    public void updateBook(Book book) {
        validateBook(book);
        bookDAO.findById(book.getId()).orElseThrow(() -> new BookNotFoundException("Cannot update: book not found"));
        bookDAO.update(book);
    }

    /**
     * Removes a book from the listing (admin only)
     */
    public void deleteBook(int bookId) {
        bookDAO.findById(bookId).orElseThrow(() -> new BookNotFoundException("Cannot delete: book not found"));
        bookDAO.delete(bookId);
        logger.info("Book deleted: {}", bookId);
    }

    /**
     * Updates the stock quantity for a book
     */
    public void updateStock(int bookId, int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        bookDAO.updateStock(bookId, quantity);
    }

    private void validateBook(Book book) {
        if (book.getTitle() == null || book.getTitle().isBlank())
            throw new IllegalArgumentException("Book title is required");
        if (book.getAuthor() == null || book.getAuthor().isBlank())
            throw new IllegalArgumentException("Book author is required");
        if (book.getGenre() == null || book.getGenre().isBlank())
            throw new IllegalArgumentException("Book genre is required");
        if (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Book price must be greater than zero");
        if (book.getStockQuantity() < 0)
            throw new IllegalArgumentException("Stock quantity cannot be negative");
    }

}
