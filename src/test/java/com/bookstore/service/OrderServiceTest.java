package com.bookstore.service;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.OrderDAO;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderDAO mockOrderDAO;

    @Mock
    private BookDAO mockBookDAO;

    private OrderService orderService;
    private CartService cartService;
    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() throws Exception {
        orderService = new OrderService();

        Field orderDAOField = OrderService.class.getDeclaredField("orderDAO");
        orderDAOField.setAccessible(true);
        orderDAOField.set(orderService, mockOrderDAO);

        Field bookDAOField = OrderService.class.getDeclaredField("bookDAO");
        bookDAOField.setAccessible(true);
        bookDAOField.set(orderService, mockBookDAO);

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("john_doe");
        testUser.setRole(User.Role.CUSTOMER);
        testUser.setStatus(User.Status.ACTIVE);

        testBook = new Book(1, "Test Book", "Test Author", "Fiction", "1234567890123",
                new BigDecimal("20.00"), 10, "Description", "Publisher", 2020);

        cartService = new CartService();
    }

    // placeOrder
    @Test
    void placeOrder_shouldCreateOrderAndReturnIt_whenCartIsValid() {
        cartService.addToCart(testBook, 2);

        when(mockBookDAO.findById(1)).thenReturn(Optional.of(testBook));

        Order savedOrder = new Order(testUser.getId(), testUser.getUsername());
        savedOrder.setId(100);
        savedOrder.setStatus(Order.Status.CONFIRMED);
        when(mockOrderDAO.create(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.placeOrder(testUser, cartService, "123 Main St");

        assertNotNull(result);
        assertEquals(100, result.getId());
        assertEquals(Order.Status.CONFIRMED, result.getStatus());
        verify(mockOrderDAO).create(any(Order.class));
        verify(mockBookDAO).updateStock(eq(1), eq(8)); // 10 - 2 = 8
        assertTrue(cartService.isEmpty());
    }

    @Test
    void placeOrder_shouldThrow_whenCartIsEmpty() {
        assertThrows(IllegalStateException.class,
                () -> orderService.placeOrder(testUser, cartService, "123 Main St"));
    }

    @Test
    void placeOrder_shouldThrow_whenStockIsInsufficient() {
        cartService.addToCart(testBook, 2);

        Book lowStockBook = new Book(1, "Test Book", "Test Author", "Fiction", "1234567890123",
                new BigDecimal("20.00"), 1, "Description", "Publisher", 2020);
        when(mockBookDAO.findById(1)).thenReturn(Optional.of(lowStockBook));

        assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(testUser, cartService, "123 Main St"));
    }

    @Test
    void placeOrder_shouldThrow_whenBookNoLongerExists() {
        cartService.addToCart(testBook, 1);
        when(mockBookDAO.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> orderService.placeOrder(testUser, cartService, "123 Main St"));
    }

    @Test
    void placeOrder_shouldClearCartOnSuccess() {
        cartService.addToCart(testBook, 1);
        when(mockBookDAO.findById(1)).thenReturn(Optional.of(testBook));
        when(mockOrderDAO.create(any())).thenReturn(new Order(testUser.getId(), testUser.getUsername()));

        orderService.placeOrder(testUser, cartService, "123 Main St");
        assertTrue(cartService.isEmpty());
    }

    @Test
    void placeOrder_shouldSetOrderStatusToConfirmed() {
        cartService.addToCart(testBook, 1);
        when(mockBookDAO.findById(1)).thenReturn(Optional.of(testBook));

        when(mockOrderDAO.create(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1);
            return o;
        });

        Order result = orderService.placeOrder(testUser, cartService, "123 Main St");
        assertEquals(Order.Status.CONFIRMED, result.getStatus());
    }

    // getOrdersForUser
    @Test
    void getOrdersForUser_shouldReturnOrdersFromDAO() {
        Order order = new Order(1, "john_doe");
        when(mockOrderDAO.findByUserId(1)).thenReturn(List.of(order));
        List<Order> result = orderService.getOrdersForUser(1);
        assertEquals(1, result.size());
        verify(mockOrderDAO).findByUserId(1);
    }

    @Test
    void getOrdersForUser_shouldReturnEmptyList_whenNoOrders() {
        when(mockOrderDAO.findByUserId(99)).thenReturn(List.of());
        List<Order> result = orderService.getOrdersForUser(99);
        assertTrue(result.isEmpty());
    }

    // getAllOrders
    @Test
    void getAllOrders_shouldReturnAllOrdersFromDAO() {
        Order o1 = new Order(1, "john_doe");
        Order o2 = new Order(2, "jane_doe");
        when(mockOrderDAO.findAll()).thenReturn(List.of(o1, o2));
        List<Order> result = orderService.getAllOrders();
        assertEquals(2, result.size());
        verify(mockOrderDAO).findAll();
    }

    // updateOrderStatus
    @Test
    void updateOrderStatus_shouldCallDAOWithCorrectStatus() {
        doNothing().when(mockOrderDAO).updateStatus(1, Order.Status.SHIPPED);
        orderService.updateOrderStatus(1, Order.Status.SHIPPED);
        verify(mockOrderDAO).updateStatus(1, Order.Status.SHIPPED);
    }

    @Test
    void updateOrderStatus_shouldWorkForAllStatuses() {
        for (Order.Status status : Order.Status.values()) {
            doNothing().when(mockOrderDAO).updateStatus(anyInt(), eq(status));
            assertDoesNotThrow(() -> orderService.updateOrderStatus(1, status));
        }
    }
}