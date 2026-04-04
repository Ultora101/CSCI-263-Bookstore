package com.bookstore.model;

import java.math.BigDecimal;

/**
 * Book object creation.
 * Requires everything for the book and inventory info
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class Book {

    private int id;
    private String title;
    private String author;
    private String genre;
    private String isbn; // This is the International Standard book number. better identification
    private BigDecimal price;
    private int stockQuantity;
    private String description;
    private String publisher;
    private int publishYear;
    private String coverImagePath;

    public Book() {
    }

    public Book(int id, String title, String author, String genre, String isbn, BigDecimal price, int stockQuantity,
            String description, String publisher, int publishYear) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isbn = isbn;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
        this.publisher = publisher;
        this.publishYear = publishYear;
    }

    // Checking if in stock
    public boolean isAvailable() {
        return stockQuantity > 0;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    @Override
    public String toString() {
        return "Book{id=" + id + ", title='" + title + "', author='" + author + "', price=" + price + ", stock="
                + stockQuantity + "}";
    }
}
