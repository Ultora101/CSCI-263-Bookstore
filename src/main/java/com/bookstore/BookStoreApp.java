package com.bookstore;

import com.bookstore.dao.DatabaseManager;
import com.bookstore.service.AuthService;
import com.bookstore.service.BookService;
import com.bookstore.service.CartService;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import com.bookstore.ui.LoginScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the book store
 * Initializes services and launches javafx gui
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class BookStoreApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(BookStoreApp.class);

    // shared service instances
    private static AuthService authService;
    private static BookService bookService;
    private static CartService cartService;
    private static OrderService orderService;
    private static UserService userService;

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Online Bookstore App");

        // initialize services
        authService = new AuthService();
        bookService = new BookService();
        cartService = new CartService();
        orderService = new OrderService();
        userService = new UserService();

        // Launch login screen
        LoginScreen loginScreen = new LoginScreen(primaryStage, authService, bookService, cartService, orderService,
                userService);
        Scene scene = new Scene(loginScreen.getRoot(), 1100, 720);

        primaryStage.setTitle("Online Book Store");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            logger.info("Application shutting down");
            DatabaseManager.getInstance().closeConnection();
        });

        logger.info("Application started successfully");
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // getters for services
    public static AuthService getAuthService() {
        return authService;
    }

    public static BookService getBookService() {
        return bookService;
    }

    public static CartService getCartService() {
        return cartService;
    }

    public static OrderService getOrderService() {
        return orderService;
    }

    public static UserService getUserService() {
        return userService;
    }

}
