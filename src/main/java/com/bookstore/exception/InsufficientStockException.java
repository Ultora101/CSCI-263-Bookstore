package com.bookstore.exception;

/**
 * Exception thrown when requested quantity exceeds avalable stock
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
