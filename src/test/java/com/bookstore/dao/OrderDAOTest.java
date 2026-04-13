package com.bookstore.dao;

import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OrderDAO using the actual SQLite database.
 * Orders created during tests are tracked for cleanup verification.
 * Uses seeded user (john_doe, id=2) and seeded book (id=1) as foreign keys.
 */
public class OrderDAOTest {

    private OrderDAO orderDAO;
    private UserDAO userDAO;
    private BookDAO bookDAO;

    private int testUserId;
    private int testBookId;
    private int createdOrderId = -1;

    @BeforeEach
    void setUp() {
        orderDAO = new OrderDAO();
        userDAO = new UserDAO();
        bookDAO = new BookDAO();

        // Resolve seeded user and book IDs dynamically
        testUserId = userDAO.findByUsername("john_doe")
                .orElseThrow(() -> new IllegalStateException("Seeded user john_doe not found"))
                .getId();

        testBookId = bookDAO.findAll().get(0).getId();
    }

    @AfterEach
    void tearDown() {
        createdOrderId = -1;
    }

    private Order buildTestOrder() {
        Order order = new Order(testUserId, "john_doe");
        order.setShippingAddress("123 Test Street, Test City, TS 00000");
        order.setSubtotal(new BigDecimal("39.98"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTaxAmount(new BigDecimal("3.20"));
        order.setTotalAmount(new BigDecimal("43.18"));
        order.setStatus(Order.Status.CONFIRMED);

        OrderItem item = new OrderItem(testBookId, "Test Book", "Test Author", 2, new BigDecimal("19.99"));
        order.getItems().add(item);

        return order;
    }

    // create
    @Test
    void create_shouldPersistOrderAndAssignId() {
        Order order = buildTestOrder();
        Order created = orderDAO.create(order);
        createdOrderId = created.getId();

        assertTrue(created.getId() > 0, "Created order should have a positive ID");
    }

    @Test
    void create_shouldPersistOrderItems() {
        Order order = buildTestOrder();
        Order created = orderDAO.create(order);
        createdOrderId = created.getId();

        Optional<Order> found = orderDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getItems().size());
        assertEquals(testBookId, found.get().getItems().get(0).getBookId());
    }

    @Test
    void create_shouldPersistCorrectAmounts() {
        Order order = buildTestOrder();
        Order created = orderDAO.create(order);
        createdOrderId = created.getId();

        Optional<Order> found = orderDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(0, new BigDecimal("39.98").compareTo(found.get().getSubtotal()));
        assertEquals(0, new BigDecimal("43.18").compareTo(found.get().getTotalAmount()));
    }

    @Test
    void create_shouldPersistShippingAddress() {
        Order order = buildTestOrder();
        Order created = orderDAO.create(order);
        createdOrderId = created.getId();

        Optional<Order> found = orderDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("123 Test Street, Test City, TS 00000", found.get().getShippingAddress());
    }

    // findById
    @Test
    void findById_shouldReturnOrder_whenExists() {
        Order created = orderDAO.create(buildTestOrder());
        createdOrderId = created.getId();

        Optional<Order> result = orderDAO.findById(created.getId());
        assertTrue(result.isPresent());
        assertEquals(testUserId, result.get().getUserId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        Optional<Order> result = orderDAO.findById(Integer.MAX_VALUE);
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldIncludeOrderItems() {
        Order created = orderDAO.create(buildTestOrder());
        createdOrderId = created.getId();

        Optional<Order> result = orderDAO.findById(created.getId());
        assertTrue(result.isPresent());
        assertFalse(result.get().getItems().isEmpty());
    }

    @Test
    void findById_shouldPopulateUsername() {
        Order created = orderDAO.create(buildTestOrder());
        createdOrderId = created.getId();

        Optional<Order> result = orderDAO.findById(created.getId());
        assertTrue(result.isPresent());
        assertEquals("john_doe", result.get().getUsername());
    }

    // findByUserId
    @Test
    void findByUserId_shouldReturnOrdersForUser() {
        Order created = orderDAO.create(buildTestOrder());
        createdOrderId = created.getId();

        List<Order> orders = orderDAO.findByUserId(testUserId);
        assertFalse(orders.isEmpty());
        assertTrue(orders.stream().anyMatch(o -> o.getId() == created.getId()));
    }

    @Test
    void findByUserId_shouldReturnEmpty_whenUserHasNoOrders() {
        int adminId = userDAO.findByUsername("admin")
                .orElseThrow(() -> new IllegalStateException("admin not found"))
                .getId();
        List<Order> orders = orderDAO.findByUserId(adminId);
        assertTrue(orders.isEmpty());
    }

    @Test
    void findByUserId_shouldIncludeOrderItems() {
        Order created = orderDAO.create(buildTestOrder());
        createdOrderId = created.getId();

        List<Order> orders = orderDAO.findByUserId(testUserId);
        Order found = orders.stream()
                .filter(o -> o.getId() == created.getId())
                .findFirst()
                .orElseThrow();
        assertFalse(found.getItems().isEmpty());
    }

    // findAll
    @Test
    void findAll_shouldReturnAllOrders() {
        Order created = orderDAO.create(buildTestOrder());
        createdOrderId = created.getId();

        List<Order> all = orderDAO.findAll();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(o -> o.getId() == created.getId()));
    }

    @Test
    void findAll_shouldIncludeOrderItems() {
        Order created = orderDAO.create(buildTestOrder());
        createdOrderId = created.getId();

        List<Order> all = orderDAO.findAll();
        Order found = all.stream()
                .filter(o -> o.getId() == created.getId())
                .findFirst()
                .orElseThrow();
        assertFalse(found.getItems().isEmpty());
    }

    // updateStatus
    @Test
    void updateStatus_shouldChangeOrderStatus() {
        Order created = orderDAO.create(buildTestOrder());
        createdOrderId = created.getId();

        orderDAO.updateStatus(created.getId(), Order.Status.SHIPPED);

        Optional<Order> updated = orderDAO.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals(Order.Status.SHIPPED, updated.get().getStatus());
    }

    @Test
    void updateStatus_shouldWorkForAllStatusValues() {
        Order created = orderDAO.create(buildTestOrder());
        createdOrderId = created.getId();

        for (Order.Status status : Order.Status.values()) {
            assertDoesNotThrow(() -> orderDAO.updateStatus(created.getId(), status));
            Optional<Order> updated = orderDAO.findById(created.getId());
            assertTrue(updated.isPresent());
            assertEquals(status, updated.get().getStatus());
        }
    }

    @Test
    void updateStatus_shouldUpdateTimestamp() {
        Order created = orderDAO.create(buildTestOrder());
        createdOrderId = created.getId();

        orderDAO.updateStatus(created.getId(), Order.Status.DELIVERED);

        Optional<Order> updated = orderDAO.findById(created.getId());
        assertTrue(updated.isPresent());
        assertNotNull(updated.get().getUpdatedAt());
    }
}