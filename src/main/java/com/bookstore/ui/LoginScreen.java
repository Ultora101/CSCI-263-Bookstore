package com.bookstore.ui;

import com.bookstore.exception.AuthenticationException;
import com.bookstore.model.User;
import com.bookstore.service.*;
import com.bookstore.util.AlertUtil;
import com.bookstore.util.StyleManager;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Login and registration screen
 * 
 * @author Lyle Voth
 * @version 1.0
 */
public class LoginScreen {

    private final Stage stage;
    private final AuthService authService;
    private final BookService bookService;
    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    private BorderPane root;
    private StackPane centerPane;

    public LoginScreen(Stage stage, AuthService authService, BookService bookService, CartService cartService,
            OrderService orderService, UserService userService) {
        this.stage = stage;
        this.authService = authService;
        this.bookService = bookService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + StyleManager.PRIMARY + ";");

        // left branding panel
        VBox brandPanel = buildBrandPanel();
        root.setLeft(brandPanel);

        // Right form panel
        centerPane = new StackPane();
        centerPane.setStyle("-fx-background-color: " + StyleManager.LIGHT_BG + ";");
        centerPane.setPrefWidth(480);
        showLoginForm();
        root.setCenter(centerPane);
    }

    private VBox buildBrandPanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(440);
        panel.setStyle("-fx-background-color: " + StyleManager.PRIMARY + ";");
        panel.setPadding(new Insets(60));

        Label title = new Label("Book Store Application");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label tagline = new Label("Books Galore to Explore");
        tagline.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255,255,255,0.75);");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.2);");
        sep.setMaxWidth(200);

        VBox features = new VBox(12);
        features.setAlignment(Pos.CENTER_LEFT);
        for (String f : new String[] { "Browse 1000+ books", "Secure checkout", "Order tracking", "Admin dashboard" }) {
            Label lbl = new Label(f);
            lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.85);");
            features.getChildren().add(lbl);
        }

        panel.getChildren().addAll(title, tagline, sep, features);
        return panel;
    }

    private void showLoginForm() {
        VBox form = new VBox(18);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(50, 60, 50, 60));
        form.setMaxWidth(400);

        Label heading = new Label("Welcome Back");
        heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + StyleManager.TEXT_DARK + ";");
        Label sub = new Label("Sign in to your account");
        sub.setStyle(StyleManager.mutedLabel());

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(StyleManager.textField());
        usernameField.setPrefHeight(42);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(StyleManager.textField());
        passwordField.setPrefHeight(42);

        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle(StyleManager.primaryButton());
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(44);

        Label orLabel = new Label("Don't have an account?");
        orLabel.setStyle(StyleManager.mutedLabel());

        Button registerBtn = new Button("Create Account");
        registerBtn.setStyle(StyleManager.outlineButton());
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(42);

        Label hint = new Label("Demo: admin/adminPass or john_doe/customer123");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + StyleManager.TEXT_MUTED + "; -fx-font-style: italic;");

        loginBtn.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));
        passwordField.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));
        registerBtn.setOnAction(e -> showRegisterForm());

        // Hover effects
        loginBtn.setOnMouseEntered(e -> loginBtn
                .setStyle(StyleManager.primaryButton() + "-fx-background-color: " + StyleManager.ACCENT_HOVER + ";"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(StyleManager.primaryButton()));

        form.getChildren().addAll(heading, sub, usernameField, passwordField, loginBtn, orLabel, registerBtn, hint);

        StackPane wrapper = new StackPane(form);
        wrapper.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 4);");
        wrapper.setMaxWidth(400);
        wrapper.setMaxHeight(520);

        StackPane outer = new StackPane(wrapper);
        outer.setPadding(new Insets(40));
        centerPane.getChildren().setAll(outer);
    }

    private void showRegisterForm() {
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40, 60, 40, 60));
        form.setMaxWidth(400);

        Label heading = new Label("Create Account");
        heading.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + StyleManager.TEXT_DARK + ";");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.setStyle(StyleManager.textField());
        fullNameField.setPrefHeight(40);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (letters, numbers, _)");
        usernameField.setStyle(StyleManager.textField());
        usernameField.setPrefHeight(40);

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setStyle(StyleManager.textField());
        emailField.setPrefHeight(40);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 6 characters)");
        passwordField.setStyle(StyleManager.textField());
        passwordField.setPrefHeight(40);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setStyle(StyleManager.textField());
        confirmField.setPrefHeight(40);

        Button registerBtn = new Button("Create Account");
        registerBtn.setStyle(StyleManager.successButton());
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(44);

        Button backBtn = new Button("Back to Login");
        backBtn.setStyle(StyleManager.outlineButton());
        backBtn.setMaxWidth(Double.MAX_VALUE);

        registerBtn.setOnAction(e -> handleRegister(
                fullNameField.getText(), usernameField.getText(),
                emailField.getText(), passwordField.getText(), confirmField.getText()));
        backBtn.setOnAction(e -> showLoginForm());

        form.getChildren().addAll(heading, fullNameField, usernameField, emailField, passwordField, confirmField,
                registerBtn, backBtn);

        StackPane wrapper = new StackPane(form);
        wrapper.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 4);");
        wrapper.setMaxWidth(400);

        StackPane outer = new StackPane(wrapper);
        outer.setPadding(new Insets(30));
        centerPane.getChildren().setAll(outer);
    }

    private void handleLogin(String username, String password) {
        try {
            User user = authService.login(username, password);
            openMainApp(user);
        } catch (AuthenticationException ex) {
            AlertUtil.showError("Login Failed", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Error", "An unexpected error occurred: " + ex.getMessage());
        }
    }

    private void handleRegister(String fullName, String username, String email, String password, String confirm) {
        if (!password.equals(confirm)) {
            AlertUtil.showError("Validation Error", "Passwords do not match");
            return;
        }
        try {
            authService.register(username, password, email, fullName);
            AlertUtil.showSuccess("Account created! You can now login.");
            showLoginForm();
        } catch (AuthenticationException ex) {
            AlertUtil.showError("Registration Failed", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Error", "Registration fialed: " + ex.getMessage());
        }
    }

    private void openMainApp(User user) {
        if (user.isAdmin()) {
            AdminDashboard dashboard = new AdminDashboard(stage, authService, bookService, orderService, userService,
                    cartService);
            stage.getScene().setRoot(dashboard.getRoot());
        } else {
            cartService.loadCartForUser(user.getId());
            CustomerDashboard dashboard = new CustomerDashboard(stage, authService, bookService, cartService,
                    orderService, this);
            stage.getScene().setRoot(dashboard.getRoot());
        }
        stage.setTitle("Book Store" + (user.isAdmin() ? " - Admin" : " - " + user.getFullName()));
    }

    public Parent getRoot() {
        return root;
    }

}
