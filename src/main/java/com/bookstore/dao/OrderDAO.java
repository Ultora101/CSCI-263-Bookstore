package com.bookstore.dao;

import com.bookstore.exception.DatabaseException;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Order entities
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class OrderDAO {

    private static final Logger logger = LoggerFactory.getLogger(OrderDAO.class);
    private final DatabaseManager dbManager;

    public OrderDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public Order create(Order order) {
        String sql = "INSERT INTO orders (user_id, subtotal, discount_amount, tax_amount, total_amount, status, shipping_address) VALUES (?,?,?,?,?,?,?)";
        Connection conn = dbManager.getConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, order.getUserId());
                ps.setDouble(2, order.getSubtotal().doubleValue());
                ps.setDouble(3, order.getDiscountAmount().doubleValue());
                ps.setDouble(4, order.getTaxAmount().doubleValue());
                ps.setDouble(5, order.getTotalAmount().doubleValue());
                ps.setString(6, order.getStatus().name());
                ps.setString(7, order.getShippingAddress());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next())
                    order.setId(keys.getInt(1));
            }
            insertOrderItems(conn, order);
            conn.commit();
            conn.setAutoCommit(true);
            logger.info("Created order #{} for user {}", order.getId(), order.getUserId());
            return order;
        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                /* ignore */ }
            logger.error("Error creating order", e);
            throw new DatabaseException("Failed to create order", e);
        }
    }

    private void insertOrderItems(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, book_id, book_title, book_author, quantity, unit_price) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderItem item : order.getItems()) {
                ps.setInt(1, order.getId());
                ps.setInt(2, item.getBookId());
                ps.setString(3, item.getBookTitle());
                ps.setString(4, item.getBookAuthor());
                ps.setInt(5, item.getQuantity());
                ps.setDouble(6, item.getUnitPrice().doubleValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Order> findByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username FROM orders o JOIN users u ON o.user_id=u.id WHERE o.user_id=? ORDER BY o.created_at DESC";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order order = mapRow(rs);
                order.setItems(findOrderItems(order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve orders", e);
        }
        return orders;
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username FROM orders o JOIN users u ON o.user_id=u.id ORDER BY o.created_at DESC";
        try (Statement stmt = dbManager.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order order = mapRow(rs);
                order.setItems(findOrderItems(order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all orders", e);
        }
        return orders;
    }

    public Optional<Order> findById(int id) {
        String sql = "SELECT o.*, u.username FROM orders o JOIN users u ON o.user_id=u.id WHERE o.id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = mapRow(rs);
                order.setItems(findOrderItems(id));
                return Optional.of(order);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find order", e);
        }
        return Optional.empty();
    }

    public void updateStatus(int orderId, Order.Status status) {
        String sql = "UPDATE orders SET status=?, updated_at=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, LocalDateTime.now().toString());
            ps.setInt(3, orderId);
            ps.executeUpdate();
            logger.info("Order #{} status updated to {}", orderId, status);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update order status", e);
        }
    }

    private List<OrderItem> findOrderItems(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setBookId(rs.getInt("book_id"));
                item.setBookTitle(rs.getString("book_title"));
                item.setBookAuthor(rs.getString("book_author"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(BigDecimal.valueOf(rs.getDouble("unit_price")));
                items.add(item);
            }
        }
        return items;
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setUsername(rs.getString("username"));
        order.setSubtotal(BigDecimal.valueOf(rs.getDouble("subtotal")));
        order.setDiscountAmount(BigDecimal.valueOf(rs.getDouble("discount_amount")));
        order.setTaxAmount(BigDecimal.valueOf(rs.getDouble("tax_amount")));
        order.setTotalAmount(BigDecimal.valueOf(rs.getDouble("total_amount")));
        order.setStatus(Order.Status.valueOf(rs.getString("status")));
        order.setShippingAddress(rs.getString("shipping_address"));
        String created = rs.getString("created_at");
        if (created != null)
            order.setCreatedAt(LocalDateTime.parse(created.replace(" ", "T")));
        String updated = rs.getString("updated_at");
        if (updated != null)
            order.setUpdatedAt(LocalDateTime.parse(updated.replace(" ", "T")));
        return order;
    }

}
