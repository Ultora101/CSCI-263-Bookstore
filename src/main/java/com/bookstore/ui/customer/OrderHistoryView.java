package com.bookstore.ui.customer;

import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.service.AuthService;
import com.bookstore.service.OrderService;
import com.bookstore.util.StyleManager;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Customer order history view.
 */
public class OrderHistoryView {

    private final OrderService orderService;
    private final AuthService authService;
    private VBox root;

    public OrderHistoryView(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
        buildUI();
    }

    private void buildUI() {
        root = new VBox();
        root.setStyle("-fx-background-color: " + StyleManager.LIGHT_BG + ";");

        HBox header = new HBox();
        header.setPadding(new Insets(24));
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent " + StyleManager.BORDER
                + " transparent;");
        Label title = new Label("My Orders");
        title.setStyle(StyleManager.pageTitle());
        header.getChildren().add(title);

        List<Order> orders = orderService.getOrdersForUser(authService.getCurrentUser().getId());

        VBox ordersList = new VBox(14);
        ordersList.setPadding(new Insets(20));

        if (orders.isEmpty()) {
            StackPane empty = new StackPane();
            empty.setPrefHeight(300);
            VBox msg = new VBox(12);
            msg.setAlignment(Pos.CENTER);
            Label txt = new Label("No orders yet");
            txt.setStyle("-fx-font-size: 18px; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");
            msg.getChildren().add(txt);
            empty.getChildren().add(msg);
            ordersList.getChildren().add(empty);
        } else {
            for (Order order : orders) {
                ordersList.getChildren().add(buildOrderCard(order));
            }
        }

        ScrollPane scroll = new ScrollPane(ordersList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + StyleManager.LIGHT_BG + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
    }

    private VBox buildOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle(StyleManager.card());

        // Header row
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label orderNum = new Label("Order #" + order.getId());
        orderNum.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label statusBadge = new Label(order.getStatus().name());
        statusBadge.setStyle(getStatusStyle(order.getStatus()));
        Label dateLbl = new Label(order.getCreatedAt() != null
                ? order.getCreatedAt().toLocalDate().toString()
                : "N/A");
        dateLbl.setStyle(StyleManager.mutedLabel());
        headerRow.getChildren().addAll(orderNum, spacer, statusBadge, dateLbl);

        // Items
        VBox itemsList = new VBox(4);
        for (OrderItem item : order.getItems()) {
            HBox itemRow = new HBox(8);
            Label itemName = new Label(item.getBookTitle() + " x " + item.getQuantity());
            itemName.setStyle("-fx-font-size: 13px;");
            Region sp2 = new Region();
            HBox.setHgrow(sp2, Priority.ALWAYS);
            Label itemPrice = new Label(String.format("$%.2f", item.getSubtotal().doubleValue()));
            itemPrice.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
            itemRow.getChildren().addAll(itemName, sp2, itemPrice);
            itemsList.getChildren().add(itemRow);
        }

        Separator sep = new Separator();

        // Totals
        HBox totalsRow = new HBox(12);
        totalsRow.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        VBox totals = new VBox(2);
        totals.setAlignment(Pos.CENTER_RIGHT);
        if (order.getDiscountAmount().doubleValue() > 0) {
            Label disc = new Label(String.format("Discount: -$%.2f", order.getDiscountAmount().doubleValue()));
            disc.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleManager.SUCCESS + ";");
            totals.getChildren().add(disc);
        }
        Label tax = new Label(String.format("Tax: $%.2f", order.getTaxAmount().doubleValue()));
        tax.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");
        Label total = new Label(String.format("Total: $%.2f", order.getTotalAmount().doubleValue()));
        total.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + StyleManager.ACCENT + ";");
        totals.getChildren().addAll(tax, total);

        if (order.getShippingAddress() != null) {
            Label addr = new Label(order.getShippingAddress());
            addr.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");
            totalsRow.getChildren().addAll(addr, sp, totals);
        } else {
            totalsRow.getChildren().addAll(sp, totals);
        }

        card.getChildren().addAll(headerRow, itemsList, sep, totalsRow);
        return card;
    }

    private String getStatusStyle(Order.Status status) {
        return switch (status) {
            case DELIVERED -> StyleManager.badgeSuccess();
            case CONFIRMED, PROCESSING -> StyleManager.badgeInfo();
            case SHIPPED -> StyleManager.badgeWarning();
            case CANCELLED -> StyleManager.badgeDanger();
            default -> StyleManager.badgeInfo();
        };
    }

    public Parent getRoot() {
        return root;
    }
}
