package com.bookstore.dao;

import com.bookstore.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Manages the SQLite database connection and initilizes the schema
 * Uses the singleton pattern to provide a single connection through the app
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:sqlite:data/bookstore.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Failed to get database connection", e);
            throw new DatabaseException("Cannot connect to database", e);
        }
        return connection;
    }

    private void initializeDatabase() {
        try {
            java.io.File dataDir = new java.io.File("data");
            if (!dataDir.exists())
                dataDir.mkdirs();

            connection = DriverManager.getConnection(DB_URL);
            createTables();
            seedData();
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Database initiailization failed", e);
            throw new DatabaseException("Failed to initialize database", e);
        }
    }

    private void createTables() throws SQLException {
        String[] ddl = {
                // Users table
                """
                        CREATE TABLE IF NOT EXISTS users (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT UNIQUE NOT NULL,
                            password_hash TEXT NOT NULL,
                            email TEXT UNIQUE NOT NULL,
                            full_name TEXT NOT NULL,
                            role TEXT NOT NULL DEFAULT 'CUSTOMER',
                            status TEXT NOT NULL DEFAULT 'ACTIVE',
                            created_at TEXT NOT NULL DEFAULT (datetime('now')),
                            last_login TEXT
                        )
                        """,
                // Books table
                """
                        CREATE TABLE IF NOT EXISTS books (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            title TEXT NOT NULL,
                            author TEXT NOT NULL,
                            genre TEXT NOT NULL,
                            isbn TEXT UNIQUE,
                            price REAL NOT NULL,
                            stock_quantity INTEGER NOT NULL DEFAULT 0,
                            description TEXT,
                            publisher TEXT,
                            publish_year INTEGER,
                            cover_image_path TEXT
                        )
                        """,
                // Orders table
                """
                        CREATE TABLE IF NOT EXISTS orders (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            subtotal REAL NOT NULL,
                            discount_amount REAL NOT NULL DEFAULT 0,
                            tax_amount REAL NOT NULL DEFAULT 0,
                            total_amount REAL NOT NULL,
                            status TEXT NOT NULL DEFAULT 'PENDING',
                            shipping_address TEXT,
                            created_at TEXT NOT NULL DEFAULT (datetime('now')),
                            updated_at TEXT NOT NULL DEFAULT (datetime('now')),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                        """,
                // Order items table
                """
                        CREATE TABLE IF NOT EXISTS order_items (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            order_id INTEGER NOT NULL,
                            book_id INTEGER NOT NULL,
                            book_title TEXT NOT NULL,
                            book_author TEXT NOT NULL,
                            quantity INTEGER NOT NULL,
                            unit_price REAL NOT NULL,
                            FOREIGN KEY (order_id) REFERENCES orders(id),
                            FOREIGN KEY (book_id) REFERENCES books(id)
                        )
                        """
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : ddl) {
                stmt.execute(sql);
            }
        }
        logger.info("Database tables created/verified");
    }

    public void seedData() throws SQLException {
        // Check if data exsists already
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0)
                return;
        }

        logger.info("Seeding initial data...");

        // seeding admin user (Password: adminPass)
        String adminHash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, "adminPass".toCharArray());
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (username, password_hash, email, full_name, role, status) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, "admin");
            ps.setString(2, adminHash);
            ps.setString(3, "admin@bookstore.com");
            ps.setString(4, "System Administrator");
            ps.setString(5, "ADMIN");
            ps.setString(6, "ACTIVE");
            ps.executeUpdate();
        }

        // Seed customer user (password: customer123)
        String custHash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12,
                "customer123".toCharArray());
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (username, password_hash, email, full_name, role, status) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, "john_doe");
            ps.setString(2, custHash);
            ps.setString(3, "john@example.com");
            ps.setString(4, "John Doe");
            ps.setString(5, "CUSTOMER");
            ps.setString(6, "ACTIVE");
            ps.executeUpdate();

        }

        // Seed books
        String[][] books = {
                { "The Great Gatsby", "F. Scott Fitzgerald", "Classic Fiction", "978-0-7432-7356-5", "12.99", "25",
                        "A story of wealth and love in 1920s America.", "Scribner", "1925" },
                { "To Kill a Mockingbird", "Harper Lee", "Classic Fiction", "978-0-06-112008-4", "14.99", "18",
                        "A powerful tale of racial injustice in the American South.", "J. B. Lippincott & Co.",
                        "1960" },
                { "1984", "George Orwell", "Dystopian Fiction", "978-0-452-28423-4", "11.99", "30",
                        "A chilling prophecy about the dangers of totalitarianism.", "Secker & Warburg", "1949" },
                { "The Hobbit", "J.R.R. Tolkien", "Fantasy", "978-0-547-92822-7", "16.99", "22",
                        "Bilbo Baggins goes on an unexpected adventure.", "Allen & Unwin", "1937" },
                { "Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy", "978-0-590-35340-3", "19.99",
                        "35", "A young boy discovers he is a wizard.", "Scholastic", "1997" },
                { "The Alchemist", "Paulo Coelho", "Adventure Fiction", "978-0-06-231609-7", "13.99", "20",
                        "A shepherd's journey to find his destiny.", "HarperCollins", "1988" },
                { "Dune", "Frank Herbert", "Science Fiction", "978-0-441-17271-9", "18.99", "15",
                        "An epic tale of politics and religion on a desert planet.", "Chilton Books", "1965" },
                { "The Catcher in the Rye", "J.D. Salinger", "Literary Fiction", "978-0-316-76948-0", "13.99", "12",
                        "A story of teenage rebellion and alienation.", "Little, Brown", "1951" },
                { "Pride and Prejudice", "Jane Austen", "Romance", "978-0-14-143951-8", "10.99", "28",
                        "A romantic novel of manners set in Georgian England.", "T. Egerton", "1813" },
                { "The Da Vinci Code", "Dan Brown", "Thriller", "978-0-385-50420-5", "15.99", "19",
                        "A mystery thriller involving a murder in the Louvre.", "Doubleday", "2003" },
                { "Sapiens", "Yuval Noah Harari", "Non-Fiction / History", "978-0-06-231609-8", "17.99", "24",
                        "A brief history of humankind.", "Harper", "2011" },
                { "Atomic Habits", "James Clear", "Self-Help", "978-0-7352-1129-2", "16.99", "40",
                        "An easy and proven way to build good habits.", "Avery", "2018" },
                { "The Midnight Library", "Matt Haig", "Contemporary Fiction", "978-0-525-55947-4", "14.99", "16",
                        "Between life and death lies a library of infinite possibilities.", "Canongate", "2020" },
                { "Gone Girl", "Gillian Flynn", "Thriller", "978-0-307-58836-4", "13.99", "21",
                        "A twisted psychological thriller about a missing wife.", "Crown", "2012" },
                { "Educated", "Tara Westover", "Memoir", "978-0-399-59050-4", "15.99", "14",
                        "A memoir about growing up in a survivalist family.", "Random House", "2018" }
        };

        String bookSql = "INSERT INTO books (title, author, genre, isbn, price, stock_quantity, description, publisher, publish_year) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(bookSql)) {
            for (String[] b : books) {
                ps.setString(1, b[0]);
                ps.setString(2, b[1]);
                ps.setString(3, b[2]);
                ps.setString(4, b[3]);
                ps.setDouble(5, Double.parseDouble(b[4]));
                ps.setInt(6, Integer.parseInt(b[5]));
                ps.setString(7, b[6]);
                ps.setString(8, b[7]);
                ps.setInt(9, Integer.parseInt(b[8]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
        logger.info("Seed data inserted successfully");
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }

}
