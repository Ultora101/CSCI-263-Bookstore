package com.bookstore.model;

import java.math.BigDecimal;

/**
 * This class handles a single item in a user's cart
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class CartItem {

    private int bookId;
    private String title;
    private String author;
    private BigDecimal price;
    private int quantity;

    public CartItem() {
    }

    public CartItem(int bookId, String title, String author, BigDecimal price, int quantity) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.quantity = quantity;
    }

    // gets the subtotal of the transaction
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "CartItem{bookId=" + bookId + ", title='" + title + "' qty=" + quantity + ", subtotal=" + getSubtotal()
                + "}";
    }
}
