package com.bookstore.dao;

import com.bookstore.model.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for BookDAO using the actual SQLite database.
 * The database is seeded with 15 books and 2 users on first run.
 * Test-created books are cleaned up after each test.
 */
public class BookDAOTest {

    private BookDAO bookDAO;
    private int createdBookId = -1;

    // A unique ISBN used for test books so they can be identified and cleaned up
    private static final String TEST_ISBN = "TEST-0000000001";

    @BeforeEach
    void setUp() {
        bookDAO = new BookDAO();
    }

    @AfterEach
    void tearDown() {
        // Clean up any book created during the test
        if (createdBookId > 0) {
            bookDAO.delete(createdBookId);
            createdBookId = -1;
        }
    }

    private Book buildTestBook() {
        Book book = new Book();
        book.setTitle("DAO Test Book");
        book.setAuthor("DAO Test Author");
        book.setGenre("Test Genre");
        book.setIsbn(TEST_ISBN);
        book.setPrice(new BigDecimal("9.99"));
        book.setStockQuantity(5);
        book.setDescription("Created for testing.");
        book.setPublisher("Test Publisher");
        book.setPublishYear(2024);
        return book;
    }

    // findAll
    @Test
    void findAll_shouldReturnNonEmptyList() {
        List<Book> books = bookDAO.findAll();
        assertFalse(books.isEmpty(), "Expected seeded books to exist");
    }

    @Test
    void findAll_shouldReturnBooksOrderedByTitle() {
        List<Book> books = bookDAO.findAll();
        for (int i = 0; i < books.size() - 1; i++) {
            assertTrue(books.get(i).getTitle().compareToIgnoreCase(books.get(i + 1).getTitle()) <= 0,
                    "Books should be in ascending title order");
        }
    }

    // findById
    @Test
    void findById_shouldReturnBook_whenExists() {
        // Get a known book from the seeded list
        List<Book> all = bookDAO.findAll();
        Book first = all.get(0);
        Optional<Book> result = bookDAO.findById(first.getId());
        assertTrue(result.isPresent());
        assertEquals(first.getTitle(), result.get().getTitle());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        Optional<Book> result = bookDAO.findById(Integer.MAX_VALUE);
        assertTrue(result.isEmpty());
    }

    // search
    @Test
    void search_shouldReturnResults_whenQueryMatchesTitle() {
        List<Book> results = bookDAO.search("gatsby");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(b -> b.getTitle().toLowerCase().contains("gatsby")));
    }

    @Test
    void search_shouldReturnResults_whenQueryMatchesAuthor() {
        List<Book> results = bookDAO.search("orwell");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(b -> b.getAuthor().toLowerCase().contains("orwell")));
    }

    @Test
    void search_shouldReturnResults_whenQueryMatchesGenre() {
        List<Book> results = bookDAO.search("fantasy");
        assertFalse(results.isEmpty());
    }

    @Test
    void search_shouldBeCaseInsensitive() {
        List<Book> lower = bookDAO.search("gatsby");
        List<Book> upper = bookDAO.search("GATSBY");
        assertEquals(lower.size(), upper.size());
    }

    @Test
    void search_shouldReturnEmpty_whenNoMatch() {
        List<Book> results = bookDAO.search("zzznomatch_xyz999");
        assertTrue(results.isEmpty());
    }

    // searchByField
    @Test
    void searchByField_shouldSearchByTitle() {
        List<Book> results = bookDAO.searchByField("title", "1984");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(b -> b.getTitle().contains("1984")));
    }

    @Test
    void searchByField_shouldSearchByAuthor() {
        List<Book> results = bookDAO.searchByField("author", "Austen");
        assertFalse(results.isEmpty());
    }

    @Test
    void searchByField_shouldSearchByGenre() {
        List<Book> results = bookDAO.searchByField("genre", "Romance");
        assertFalse(results.isEmpty());
    }

    @Test
    void searchByField_shouldDefaultToTitle_forUnknownField() {
        // Unknown field falls through to "title" in the switch
        List<Book> results = bookDAO.searchByField("unknownfield", "gatsby");
        assertFalse(results.isEmpty());
    }

    // findAllGenres
    @Test
    void findAllGenres_shouldReturnDistinctGenres() {
        List<String> genres = bookDAO.findAllGenres();
        assertFalse(genres.isEmpty());
        // Verify uniqueness
        long distinctCount = genres.stream().distinct().count();
        assertEquals(genres.size(), distinctCount, "Genres should be distinct");
    }

    @Test
    void findAllGenres_shouldContainSeededGenres() {
        List<String> genres = bookDAO.findAllGenres();
        assertTrue(genres.stream().anyMatch(g -> g.equalsIgnoreCase("Fantasy")));
        assertTrue(genres.stream().anyMatch(g -> g.equalsIgnoreCase("Thriller")));
    }

    // create
    @Test
    void create_shouldPersistBook_andAssignId() {
        Book book = buildTestBook();
        Book created = bookDAO.create(book);
        createdBookId = created.getId();

        assertTrue(created.getId() > 0, "Created book should have a positive ID");
        assertEquals("DAO Test Book", created.getTitle());
    }

    @Test
    void create_shouldBeRetrievable_afterCreation() {
        Book book = buildTestBook();
        Book created = bookDAO.create(book);
        createdBookId = created.getId();

        Optional<Book> found = bookDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("DAO Test Author", found.get().getAuthor());
    }

    // update
    @Test
    void update_shouldModifyExistingBook() {
        Book book = buildTestBook();
        Book created = bookDAO.create(book);
        createdBookId = created.getId();

        created.setTitle("Updated Title");
        created.setPrice(new BigDecimal("24.99"));
        bookDAO.update(created);

        Optional<Book> updated = bookDAO.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Title", updated.get().getTitle());
        assertEquals(new BigDecimal("24.99"), updated.get().getPrice());
    }

    // delete
    @Test
    void delete_shouldRemoveBook_fromDatabase() {
        Book book = buildTestBook();
        Book created = bookDAO.create(book);
        int id = created.getId();

        bookDAO.delete(id);
        createdBookId = -1; // already deleted, skip tearDown cleanup

        Optional<Book> result = bookDAO.findById(id);
        assertTrue(result.isEmpty(), "Deleted book should not be found");
    }

    // updateStock
    @Test
    void updateStock_shouldChangeStockQuantity() {
        Book book = buildTestBook();
        Book created = bookDAO.create(book);
        createdBookId = created.getId();

        bookDAO.updateStock(created.getId(), 99);

        Optional<Book> updated = bookDAO.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals(99, updated.get().getStockQuantity());
    }

    @Test
    void updateStock_shouldAllowZero() {
        Book book = buildTestBook();
        Book created = bookDAO.create(book);
        createdBookId = created.getId();

        bookDAO.updateStock(created.getId(), 0);

        Optional<Book> updated = bookDAO.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals(0, updated.get().getStockQuantity());
        assertFalse(updated.get().isAvailable());
    }
}