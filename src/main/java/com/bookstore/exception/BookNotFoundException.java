package com.bookstore.exception;

/**
 * Exception gets thrown when a request to find a book fails
 */
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
