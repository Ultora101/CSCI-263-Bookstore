# CSCI-263-Bookstore

A JavaFX desktop application for a bookstore with separate customer and admin interfaces, backed by a SQLite database.

**Author:** Lyle Voth

---

## Requirements

- Java 17
- Maven 3.6+

---

## Build and Run

```bash
# Run the application
mvn javafx:run

# Build a fat JAR
mvn package

# Run the fat JAR
java -jar target/online-book-store-1.0-SNAPSHOT.jar

# Run tests
mvn test
```

---

## Project Structure

```
src/main/java/com/bookstore/
├── BookStoreApp.java               # Application entry point
├── dao/
│   ├── DatabaseManager.java        # Singleton SQLite connection and schema init
│   ├── BookDAO.java                # Book CRUD operations
│   ├── OrderDAO.java               # Order persistence
│   └── UserDAO.java                # User persistence
├── exception/
│   ├── AuthenticationException.java
│   ├── BookNotFoundException.java
│   ├── DatabaseException.java
│   └── InsufficientStockException.java
├── model/
│   ├── Book.java
│   ├── CartItem.java
│   ├── Order.java                  # Includes Order.Status enum
│   ├── OrderItem.java
│   └── User.java                   # Includes User.Role and User.Status enums
├── service/
│   ├── AuthService.java            # Login, registration, BCrypt password hashing
│   ├── BookService.java            # Book search and inventory logic
│   ├── CartService.java            # Shopping cart management
│   ├── OrderService.java           # Order placement and retrieval
│   └── UserService.java            # User account management
├── ui/
│   ├── LoginScreen.java
│   ├── CustomerDashboard.java
│   ├── AdminDashboard.java
│   ├── customer/
│   │   ├── CatalogView.java        # Browse and search books
│   │   ├── CartView.java           # View and checkout cart
│   │   ├── OrderHistoryView.java
│   │   └── ProfileView.java
│   └── admin/
│       ├── AdminOverviewView.java  # Summary statistics
│       ├── BookManagementView.java # Add, edit, delete books
│       ├── OrderManagementView.java
│       └── UserManagementView.java # Block/unblock users
└── util/
    ├── AlertUtil.java
    └── StyleManager.java

src/test/java/com/bookstore/
├── dao/        BookDAOTest, OrderDAOTest, UserDAOTest
├── model/      BookTest, CartItemTest, OrderItemTest, OrderTest, UserTest
└── service/    AuthServiceTest, BookServiceTest, CartServiceTest,
                OrderServiceTest, UserServiceTest

src/main/resources/
└── logback.xml                     # Logging configuration

data/
└── bookstore.db                    # SQLite database (auto-created on first run)

logs/
└── bookstore.log                   # Application log output
```

---

## Dependencies

| Library              | Version  | Purpose                |
| -------------------- | -------- | ---------------------- |
| JavaFX               | 17.0.2   | GUI framework          |
| SQLite JDBC (Xerial) | 3.42.0.0 | Database driver        |
| BCrypt (Favre)       | 0.10.2   | Password hashing       |
| SLF4J                | 2.0.9    | Logging API            |
| Logback              | 1.4.11   | Logging implementation |
| JUnit 5              | 5.10.0   | Unit testing           |
| Mockito              | 5.5.0    | Mocking framework      |

---

## Features

**Customer**

- Register and log in
- Browse and search the book catalog
- Add books to cart and place orders
- View order history
- Edit profile

**Admin**

- View store overview with summary statistics
- Add, edit, and delete books; manage inventory
- View and manage all orders
- Block or unblock user accounts

---

## Database

The SQLite database is created automatically at `data/bookstore.db` on first run. `DatabaseManager` initializes the schema and seeds default data. The database is not checked into version control via `.gitignore`.

---

## Notes

- The `data/carts/` directory is used for cart persistence.
- Window minimum size is 900x600; default launch size is 1100x720.
- Passwords are hashed with BCrypt before storage; plain-text passwords are never saved.
