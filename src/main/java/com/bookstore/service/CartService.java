package com.bookstore.service;

import com.bookstore.exception.InsufficientStockException;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This service manages the shopping cart
 * uses files in order to persist cart data between sessions
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private static final String CART_DIR = "data/carts/";
    private static final double TAX_RATE = 0.08; // 8% tax
    private static final double DISCOUNT_THRESHOLD = 50.0;
    private static final double DISCOUNT_RATE = 0.10; // 10% discout over $50

    /** In-memory cart: bookId -> CartItem */
    private final Map<Integer, CartItem> cart = new HashMap<>();
    private int currentUserId = -1;

    public CartService() {
        new File(CART_DIR).mkdir();
    }

    /**
     * Loads the persistded cart for a user from file
     */
    public void loadCartForUser(int userId) {
        this.currentUserId = userId;
        cart.clear();
        File cartFile = getCartFile(userId);
        if (!cartFile.exists())
            return;

        try (BufferedReader br = new BufferedReader(new FileReader(cartFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    CartItem item = new CartItem(Integer.parseInt(parts[0]), parts[1], parts[2],
                            new BigDecimal(parts[3]), Integer.parseInt(parts[4]));
                    cart.put(item.getBookId(), item);
                }
            }
            logger.info("Loaded cart for user {}: {} items", userId, cart.size());
        } catch (IOException | NumberFormatException e) {
            logger.warn("Could not load cart for user {}: {}", userId, e.getMessage());
            cart.clear();
        }
    }

    /**
     * Persists the current cart to file for the current user
     */
    public void saveCart() {
        if (currentUserId < 0)
            return;
        File cartFile = getCartFile(currentUserId);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(cartFile))) {
            for (CartItem item : cart.values()) {
                bw.write(item.getBookId() + "|" + item.getTitle() + "|" + item.getAuthor() + "|" + item.getPrice() + "|"
                        + item.getQuantity());
                bw.newLine();
            }
            logger.debug("Cart saved for user {}", currentUserId);
        } catch (IOException e) {
            logger.error("Failed to save cartr for user {}", currentUserId, e);
        }
    }

    /**
     * Adds a book to the cart or increments quantity if already in cart
     * 
     * @param book     the book needed to add
     * @param quantity number of quantity to add
     * @throws InsufficientStockException if not enough stock
     */
    public void addToCart(Book book, int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");

        int currentQty = cart.containsKey(book.getId()) ? cart.get(book.getId()).getQuantity() : 0;
        int totalQty = currentQty + quantity;

        if (totalQty > book.getStockQuantity()) {
            throw new InsufficientStockException(
                    "Only " + book.getStockQuantity() + " copies of '" + book.getTitle() + "' in stock");
        }

        if (cart.containsKey(book.getId())) {
            cart.get(book.getId()).setQuantity(totalQty);
        } else {
            cart.put(book.getId(),
                    new CartItem(book.getId(), book.getTitle(), book.getAuthor(), book.getPrice(), quantity));
        }
        saveCart();
        logger.info("Added {} + '{}' to cart", quantity, book.getTitle());
    }

    /**
     * Removes a book entirely from cart
     */
    public void removeFromCart(int bookId) {
        CartItem removed = cart.remove(bookId);
        if (removed != null) {
            saveCart();
            logger.info("Removed book {} from cart", bookId);
        }
    }

    /**
     * updates the quantity of a cart item
     */
    public void updateQuantity(int bookId, int newQuantity) {
        if (newQuantity <= 0) {
            removeFromCart(bookId);
            return;
        }
        CartItem item = cart.get(bookId);
        if (item != null) {
            item.setQuantity(newQuantity);
            saveCart();
        }
    }

    public void clearCart() {
        cart.clear();
        saveCart();
        logger.info("Cart cleared for user {}", currentUserId);
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cart.values());
    }

    public int getItemCount() {
        return cart.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isEmpty() {
        return cart.isEmpty();
    }

    /**
     * Calculates the subtotal (before the discount and tax)
     */
    public BigDecimal getSubtotal() {
        return cart.values().stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates discount amount
     */
    public BigDecimal getDiscountAmount() {
        BigDecimal subtotal = getSubtotal();
        if (subtotal.doubleValue() >= DISCOUNT_THRESHOLD) {
            return subtotal.multiply(BigDecimal.valueOf(DISCOUNT_RATE)).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculates tax amount (applied after discount)
     */
    public BigDecimal getTaxAmount() {
        BigDecimal afterDiscount = getSubtotal().subtract(getDiscountAmount());
        return afterDiscount.multiply(BigDecimal.valueOf(TAX_RATE)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculates final total (subtotal - discount + tax)
     */
    public BigDecimal getTotal() {
        return getSubtotal().subtract(getDiscountAmount()).add(getTaxAmount()).setScale(2,
                java.math.RoundingMode.HALF_UP);
    }

    public double getTaxRate() {
        return TAX_RATE;
    }

    public double getDiscountRate() {
        return DISCOUNT_RATE;
    }

    public double getDiscountThreshold() {
        return DISCOUNT_THRESHOLD;
    }

    private File getCartFile(int userId) {
        return new File(CART_DIR + "cart_" + userId + ".txt");
    }
}
