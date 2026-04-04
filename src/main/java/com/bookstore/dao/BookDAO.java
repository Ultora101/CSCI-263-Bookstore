package com.bookstore.dao;

import com.bookstore.exception.DatabaseException;
import com.bookstore.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for book entities
 * Handles all crud operations for books in the database
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class BookDAO {

    private static final Logger logger = LoggerFactory.getLogger(BookDAO.class);
    private final DatabaseManager dbManager;

    public BookDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String sql = "STRING * FROM books ORDER BY title";
        try (Statement stmt = dbManager.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                books.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("Error fetching all books", e);
            throw new DatabaseException("Failed to retrieve books", e);
        }
        return books;
    }

    public Optional<Book> findById(int id) {
        String sql = "SELECT * FROM books WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            logger.error("Error finding book by id: {}", id, e);
            throw new DatabaseException("Failed to find book", e);
        }
        return Optional.empty();
    }

    /**
     * Searces books by title, author, or genre (case-insensitive)
     */
    public List<Book> search(String query) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(genre) LIKE ? ORDER BY title";
        String pattern = "%" + query.toLowerCase() + "%";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                books.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("Error searching books: {}", query, e);
            throw new DatabaseException("Search failed", e);
        }
        return books;
    }

    /**
     * Searches books filtered by a specific field.
     */
    public List<Book> searchByField(String field, String query) {
        List<Book> books = new ArrayList<>();
        String column = switch (field.toLowerCase()) {
            case "title" -> "title";
            case "author" -> "author";
            case "genre" -> "genre";
            default -> "title";
        };
        String sql = "SELECT * FROM books WHERE LOWER(" + column + ") LIKE ? ORDER BY title";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + query.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                books.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("Error searching books by field", e);
            throw new DatabaseException("Search failed", e);
        }
        return books;
    }

    public List<String> findAllGenres() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT DISTINCT genre FROM books ORDER BY genre";
        try (Statement stmt = dbManager.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                genres.add(rs.getString("genre"));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch genres", e);
        }
        return genres;
    }

    public Book create(Book book) {
        String sql = "INSERT INTO books (title, author, genre, isbn, price, stock_quantity, description, publisher, publish_year) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setBookParams(ps, book);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next())
                book.setId(keys.getInt(1));
            logger.info("Created book: {}", book.getTitle());
            return book;
        } catch (SQLException e) {
            logger.error("Error creating book: {}", book.getTitle(), e);
            throw new DatabaseException("Failed to create book: " + e.getMessage(), e);
        }
    }

    public void update(Book book) {
        String sql = "UPDATE books SET title=?, author=?, genre=?, isbn=?, price=?, stock_quantity=?, description=?, publisher=?, publish_year=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            setBookParams(ps, book);
            ps.setInt(10, book.getId());
            ps.executeUpdate();
            logger.info("Updated book: {}", book.getId());
        } catch (SQLException e) {
            logger.error("Error updating book: {}", book.getId(), e);
            throw new DatabaseException("Failed to update book", e);
        }
    }

    public void delete(int bookId) {
        String sql = "DELETE FROM books WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
            logger.info("Deleted book: {}", bookId);
        } catch (SQLException e) {
            logger.error("Error deleting book: {}", bookId, e);
            throw new DatabaseException("Failed to delete book", e);
        }
    }

    public void updateStock(int bookId, int newQuantity) {
        String sql = "UPDATE books SET stock_quantity=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update stock", e);
        }
    }

    private void setBookParams(PreparedStatement ps, Book book) throws SQLException {
        ps.setString(1, book.getTitle());
        ps.setString(2, book.getAuthor());
        ps.setString(3, book.getGenre());
        ps.setString(4, book.getIsbn());
        ps.setDouble(5, book.getPrice().doubleValue());
        ps.setInt(6, book.getStockQuantity());
        ps.setString(7, book.getDescription());
        ps.setString(8, book.getPublisher());
        ps.setInt(9, book.getPublishYear());
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setGenre(rs.getString("genre"));
        book.setIsbn(rs.getString("isbn"));
        book.setPrice(BigDecimal.valueOf(rs.getDouble("price")));
        book.setStockQuantity(rs.getInt("stock_quantity"));
        book.setDescription(rs.getString("description"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublishYear(rs.getInt("publish_year"));
        book.setCoverImagePath(rs.getString("cover_image_path"));
        return book;
    }
}
