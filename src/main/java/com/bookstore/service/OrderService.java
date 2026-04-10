package com.bookstore.service;

import com.bookstore.dao.BookDAO;
import com.bookstore.dao.OrderDAO;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service for managing orders and checkout.
 *
 * @author Lyle Voth
 * @version 1.0
 */
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderDAO orderDAO;
    private final BookDAO bookDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.bookDAO = new BookDAO();
    }

    /**
     * Places an order from the current cart contents
     */
    public Order placeOrder(User user, CartService cartService, String shippingAddress) {
        if (cartService.isEmpty())
            throw new IllegalStateException("Cart is empty");

        // Verify stock for all items
        for (CartItem item : cartService.getCartItems()) {
            Book book = bookDAO.findById(item.getBookId())
                    .orElseThrow(() -> new IllegalStateException("Book not found: " + item.getTitle()));
            if (book.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for '" + item.getTitle() + "'");
            }
        }

        Order order = new Order(user.getId(), user.getUsername());
        order.setShippingAddress(shippingAddress);
        order.setSubtotal(cartService.getSubtotal());
        order.setDiscountAmount(cartService.getDiscountAmount());
        order.setTaxAmount(cartService.getTaxAmount());
        order.setTotalAmount(cartService.getTotal());
        order.setStatus(Order.Status.CONFIRMED);

        for (CartItem item : cartService.getCartItems()) {
            order.getItems().add(new OrderItem(item.getBookId(), item.getTitle(), item.getAuthor(), item.getQuantity(),
                    item.getPrice()));
        }

        Order saved = orderDAO.create(order);

        // Remove items from stock
        for (CartItem item : cartService.getCartItems()) {
            Book book = bookDAO.findById(item.getBookId()).get();
            bookDAO.updateStock(book.getId(), book.getStockQuantity() - item.getQuantity());
        }

        cartService.clearCart();
        logger.info("Order #{} placed by {}", saved.getId(), user.getUsername());
        return saved;
    }

    public List<Order> getOrdersForUser(int userId) {
        return orderDAO.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    public void updateOrderStatus(int orderId, Order.Status status) {
        orderDAO.updateStatus(orderId, status);
    }
}
