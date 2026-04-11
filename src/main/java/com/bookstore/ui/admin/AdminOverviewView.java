package com.bookstore.ui.admin;

import com.bookstore.model.Order;
import com.bookstore.service.BookService;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import com.bookstore.util.StyleManager;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Admin overview dashboard with KPI stats.
 */
public class AdminOverviewView {

    private final BookService bookService;
    private final OrderService orderService;
    private final UserService userService;
    private VBox root;

    public AdminOverviewView(BookService bookService, OrderService orderService, UserService userService) {
        this.bookService = bookService;
        this.orderService = orderService;
        this.userService = userService;
        buildUI();
    }

    private void buildUI() {
        root = new VBox();
        root.setStyle("-fx-background-color: " + StyleManager.LIGHT_BG + ";");

        HBox header = new HBox();
        header.setPadding(new Insets(24));
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent " + StyleManager.BORDER
                + " transparent;");
        Label title = new Label("Dashboard Overview");
        title.setStyle(StyleManager.pageTitle());
        header.getChildren().add(title);

        // Stats
        List<Order> orders = orderService.getAllOrders();
        int totalBooks = bookService.getAllBooks().size();
        int totalUsers = userService.getAllUsers().size();
        int totalOrders = orders.size();
        double totalRevenue = orders.stream()
                .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                .mapToDouble(o -> o.getTotalAmount().doubleValue()).sum();
        long pendingOrders = orders.stream().filter(o -> o.getStatus() == Order.Status.PENDING).count();

        HBox statsRow1 = new HBox(16);
        statsRow1.setPadding(new Insets(20, 20, 0, 20));
        statsRow1.getChildren().addAll(
                statCard("Total Books", String.valueOf(totalBooks), StyleManager.INFO),
                statCard("Total Users", String.valueOf(totalUsers), StyleManager.SUCCESS),
                statCard("Total Orders", String.valueOf(totalOrders), StyleManager.WARNING),
                statCard("Revenue", String.format("$%.2f", totalRevenue), StyleManager.ACCENT));

        HBox statsRow2 = new HBox(16);
        statsRow2.setPadding(new Insets(16, 20, 0, 20));

        long confirmedOrders = orders.stream().filter(o -> o.getStatus() == Order.Status.CONFIRMED).count();
        long shippedOrders = orders.stream().filter(o -> o.getStatus() == Order.Status.SHIPPED).count();
        long deliveredOrders = orders.stream().filter(o -> o.getStatus() == Order.Status.DELIVERED).count();
        long cancelledOrders = orders.stream().filter(o -> o.getStatus() == Order.Status.CANCELLED).count();

        statsRow2.getChildren().addAll(
                statCard("Pending", String.valueOf(pendingOrders), "#9B59B6"),
                statCard("Confirmed", String.valueOf(confirmedOrders), StyleManager.INFO),
                statCard("Shipped", String.valueOf(shippedOrders), StyleManager.WARNING),
                statCard("Delivered", String.valueOf(deliveredOrders), StyleManager.SUCCESS));

        // Recent orders
        VBox recentBox = new VBox(10);
        recentBox.setPadding(new Insets(20));

        Label recentTitle = new Label("Recent Orders");
        recentTitle.setStyle(StyleManager.sectionTitle());

        TableView<Order> table = new TableView<>();
        StyleManager.styleTableView(table);
        table.setPrefHeight(250);

        TableColumn<Order, String> idCol = new TableColumn<>("Order #");
        idCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getId())));
        idCol.setPrefWidth(80);

        TableColumn<Order, String> userCol = new TableColumn<>("Customer");
        userCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getUsername()));
        userCol.setPrefWidth(140);

        TableColumn<Order, String> amtCol = new TableColumn<>("Total");
        amtCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", d.getValue().getTotalAmount().doubleValue())));
        amtCol.setPrefWidth(100);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus().name()));
        statusCol.setPrefWidth(120);

        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().toLocalDate().toString() : ""));
        dateCol.setPrefWidth(110);

        table.getColumns().addAll(idCol, userCol, amtCol, statusCol, dateCol);
        table.getItems().addAll(orders.stream().limit(10).toList());

        recentBox.getChildren().addAll(recentTitle, table);

        ScrollPane scroll = new ScrollPane(new VBox(statsRow1, statsRow2, recentBox));
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + StyleManager.LIGHT_BG + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
    }

    private VBox statCard(String label, String value, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
                "-fx-border-color: transparent transparent transparent " + color + "; " +
                "-fx-border-width: 0 0 0 4; -fx-border-radius: 10;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");

        card.getChildren().addAll(valueLbl, labelLbl);
        return card;
    }

    public Parent getRoot() {
        return root;
    }
}
