package com.bookstore.ui;

import com.bookstore.service.*;
import com.bookstore.ui.admin.*;
import com.bookstore.util.StyleManager;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Admin dashboard with sidebar navigation.
 *
 * @author Lyle Voth
 * @version 1.0
 */
public class AdminDashboard {

    private final Stage stage;
    private final AuthService authService;
    private final BookService bookService;
    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;

    private BorderPane root;
    private StackPane contentArea;
    private Button activeBtn;

    public AdminDashboard(Stage stage, AuthService authService, BookService bookService,
            OrderService orderService, UserService userService, CartService cartService) {
        this.stage = stage;
        this.authService = authService;
        this.bookService = bookService;
        this.orderService = orderService;
        this.userService = userService;
        this.cartService = cartService;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setLeft(buildSidebar());
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + StyleManager.LIGHT_BG + ";");
        root.setCenter(contentArea);
        showDashboardOverview();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(230);
        sidebar.setStyle("-fx-background-color: #1A252F;");

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(24, 20, 24, 20));
        header.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        Label appName = new Label("Admin Panel");
        appName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label adminLbl = new Label(authService.getCurrentUser().getUsername());
        adminLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.6);");
        header.getChildren().addAll(appName, adminLbl);

        VBox nav = new VBox(4);
        nav.setPadding(new Insets(16, 10, 16, 10));

        Button overviewBtn = navBtn("Overview");
        Button booksBtn = navBtn("Manage Books");
        Button ordersBtn = navBtn("Manage Orders");
        Button usersBtn = navBtn("Manage Users");

        overviewBtn.setOnAction(e -> {
            setActive(overviewBtn);
            showDashboardOverview();
        });
        booksBtn.setOnAction(e -> {
            setActive(booksBtn);
            showBookManagement();
        });
        ordersBtn.setOnAction(e -> {
            setActive(ordersBtn);
            showOrderManagement();
        });
        usersBtn.setOnAction(e -> {
            setActive(usersBtn);
            showUserManagement();
        });

        nav.getChildren().addAll(overviewBtn, booksBtn, ordersBtn, usersBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.5); " +
                "-fx-font-size: 13px; -fx-padding: 10 20; -fx-cursor: hand;");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            authService.logout();
            LoginScreen ls = new LoginScreen(stage, authService, bookService, cartService, orderService, userService);
            stage.getScene().setRoot(ls.getRoot());
            stage.setTitle("Book Haven");
        });

        VBox bottom = new VBox(logoutBtn);
        bottom.setPadding(new Insets(10));

        sidebar.getChildren().addAll(header, nav, spacer, bottom);
        activeBtn = overviewBtn;
        setActive(overviewBtn);
        return sidebar;
    }

    private Button navBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(StyleManager.navButton(false));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private void setActive(Button btn) {
        if (activeBtn != null)
            activeBtn.setStyle(StyleManager.navButton(false));
        btn.setStyle(StyleManager.navButton(true));
        activeBtn = btn;
    }

    private void showDashboardOverview() {
        AdminOverviewView view = new AdminOverviewView(bookService, orderService, userService);
        contentArea.getChildren().setAll(view.getRoot());
    }

    private void showBookManagement() {
        BookManagementView view = new BookManagementView(bookService);
        contentArea.getChildren().setAll(view.getRoot());
    }

    private void showOrderManagement() {
        OrderManagementView view = new OrderManagementView(orderService);
        contentArea.getChildren().setAll(view.getRoot());
    }

    private void showUserManagement() {
        UserManagementView view = new UserManagementView(userService, authService);
        contentArea.getChildren().setAll(view.getRoot());
    }

    public Parent getRoot() {
        return root;
    }
}
