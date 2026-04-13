package com.bookstore.ui;

import com.bookstore.model.User;
import com.bookstore.service.*;
import com.bookstore.ui.customer.*;
import com.bookstore.util.StyleManager;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Main customer dashboard with sidebar navigation.
 *
 * @author BookStore Team
 * @version 1.0
 */
public class CustomerDashboard {

    private final Stage stage;
    private final AuthService authService;
    private final BookService bookService;
    private final CartService cartService;
    private final OrderService orderService;
    private final LoginScreen loginScreen;

    private BorderPane root;
    private StackPane contentArea;
    private Button activeNavBtn;
    private Label cartBadge;

    public CustomerDashboard(Stage stage, AuthService authService, BookService bookService,
            CartService cartService, OrderService orderService, LoginScreen loginScreen) {
        this.stage = stage;
        this.authService = authService;
        this.bookService = bookService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.loginScreen = loginScreen;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();

        // Sidebar
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // Content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + StyleManager.LIGHT_BG + ";");
        root.setCenter(contentArea);

        // Show catalog by default
        showCatalog();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: " + StyleManager.SIDEBAR_BG + ";");

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(24, 20, 24, 20));
        header.setStyle("-fx-background-color: rgba(0,0,0,0.2);");
        Label appName = new Label("Book Haven");
        appName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        User user = authService.getCurrentUser();
        Label userLabel = new Label(user.getFullName());
        userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.7);");
        header.getChildren().addAll(appName, userLabel);

        // Nav buttons
        VBox nav = new VBox(4);
        nav.setPadding(new Insets(16, 10, 16, 10));

        Button catalogBtn = createNavButton("Browse Books", false);
        Button cartBtn = createCartNavButton();
        Button ordersBtn = createNavButton("My Orders", false);
        Button profileBtn = createNavButton("Profile", false);

        catalogBtn.setOnAction(e -> {
            setActive(catalogBtn);
            showCatalog();
        });
        cartBtn.setOnAction(e -> {
            setActive(cartBtn);
            showCart();
        });
        ordersBtn.setOnAction(e -> {
            setActive(ordersBtn);
            showOrders();
        });
        profileBtn.setOnAction(e -> {
            setActive(profileBtn);
            showProfile();
        });

        nav.getChildren().addAll(catalogBtn, cartBtn, ordersBtn, profileBtn);

        // Spacer + logout
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.6); " +
                "-fx-font-size: 13px; -fx-padding: 10 20; -fx-cursor: hand; -fx-alignment: CENTER-LEFT;");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> handleLogout());

        VBox bottom = new VBox(logoutBtn);
        bottom.setPadding(new Insets(10));

        sidebar.getChildren().addAll(header, nav, spacer, bottom);

        activeNavBtn = catalogBtn;
        setActive(catalogBtn);
        return sidebar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setStyle(StyleManager.navButton(active));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private Button createCartNavButton() {
        Label txt = new Label("Cart");
        txt.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 14px;");
        cartBadge = new Label(String.valueOf(cartService.getItemCount()));
        cartBadge.setStyle("-fx-background-color: " + StyleManager.ACCENT + "; -fx-text-fill: white; " +
                "-fx-background-radius: 10; -fx-font-size: 10px; -fx-padding: 2 6;");
        cartBadge.setVisible(cartService.getItemCount() > 0);
        HBox content = new HBox(8, txt, cartBadge);
        content.setAlignment(Pos.CENTER_LEFT);
        Button btn = new Button();
        btn.setGraphic(content);
        btn.setStyle(StyleManager.navButton(false));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private void setActive(Button btn) {
        if (activeNavBtn != null)
            activeNavBtn.setStyle(StyleManager.navButton(false));
        btn.setStyle(StyleManager.navButton(true));
        activeNavBtn = btn;
    }

    public void refreshCartBadge() {
        if (cartBadge != null) {
            int count = cartService.getItemCount();
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisible(count > 0);
        }
    }

    private void showCatalog() {
        CatalogView view = new CatalogView(bookService, cartService, this);
        contentArea.getChildren().setAll(view.getRoot());
    }

    private void showCart() {
        CartView view = new CartView(cartService, orderService, authService, this);
        contentArea.getChildren().setAll(view.getRoot());
    }

    private void showOrders() {
        OrderHistoryView view = new OrderHistoryView(orderService, authService);
        contentArea.getChildren().setAll(view.getRoot());
    }

    private void showProfile() {
        ProfileView view = new ProfileView(authService);
        contentArea.getChildren().setAll(view.getRoot());
    }

    private void handleLogout() {
        authService.logout();
        stage.getScene().setRoot(loginScreen.getRoot());
        stage.setTitle("Book Haven");
    }

    public Parent getRoot() {
        return root;
    }
}
