package com.bookstore.ui.customer;

import com.bookstore.model.CartItem;
import com.bookstore.model.Order;
import com.bookstore.service.AuthService;
import com.bookstore.service.CartService;
import com.bookstore.service.OrderService;
import com.bookstore.ui.CustomerDashboard;
import com.bookstore.util.AlertUtil;
import com.bookstore.util.StyleManager;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Shopping cart view with checkout functionality.
 */
public class CartView {

    private final CartService cartService;
    private final OrderService orderService;
    private final AuthService authService;
    private final CustomerDashboard dashboard;
    private VBox root;

    public CartView(CartService cartService, OrderService orderService, AuthService authService,
            CustomerDashboard dashboard) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.authService = authService;
        this.dashboard = dashboard;
        buildUI();
    }

    private void buildUI() {
        root = new VBox();
        root.setStyle("-fx-background-color: " + StyleManager.LIGHT_BG + ";");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent " + StyleManager.BORDER
                + " transparent;");
        Label title = new Label("Shopping Cart");
        title.setStyle(StyleManager.pageTitle());
        header.getChildren().add(title);

        // Body
        HBox body = new HBox(20);
        body.setPadding(new Insets(20));
        VBox.setVgrow(body, Priority.ALWAYS);

        // Cart item list
        VBox itemsPanel = buildItemsPanel();
        HBox.setHgrow(itemsPanel, Priority.ALWAYS);

        // Summary Panel
        VBox summaryPanel = buildSummaryPanel();
        summaryPanel.setPrefWidth(280);

        body.getChildren().addAll(itemsPanel, summaryPanel);
        root.getChildren().addAll(header, body);

    }

    private VBox buildItemsPanel() {
        VBox panel = new VBox(12);
        List<CartItem> items = cartService.getCartItems();

        if (items.isEmpty()) {
            StackPane empty = new StackPane();
            empty.setPrefHeight(300);
            VBox msg = new VBox(12);
            msg.setAlignment(Pos.CENTER);
            Label txt = new Label("Your cart is empty");
            txt.setStyle("-fx-font-size: 18px; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");
            Label sub = new Label("Browse the catalog to add books");
            sub.setStyle(StyleManager.mutedLabel());
            msg.getChildren().addAll(txt, sub);
            empty.getChildren().add(msg);
            panel.getChildren().add(empty);
        } else {
            for (CartItem item : items) {
                panel.getChildren().add(buildCartItemRow(item));
            }
        }
        return panel;
    }

    private HBox buildCartItemRow(CartItem item) {
        HBox row = new HBox(16);
        row.setPadding(new Insets(16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(StyleManager.card());

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label title = new Label(item.getTitle());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label author = new Label("by " + item.getAuthor());
        author.setStyle(StyleManager.mutedLabel());
        Label price = new Label(String.format("$%.2f each", item.getPrice().doubleValue()));
        price.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleManager.INFO + ";");
        info.getChildren().addAll(title, author, price);

        // Quantity spinner
        Spinner<Integer> qtySpinner = new Spinner<>(1, 99, item.getQuantity());
        qtySpinner.setPrefWidth(80);
        qtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            cartService.updateQuantity(item.getBookId(), newVal);
            dashboard.refreshCartBadge();
            refresh();
        });

        Label subtotal = new Label(String.format("$%.2f", item.getSubtotal().doubleValue()));
        subtotal.setStyle(StyleManager.priceLabel());
        subtotal.setMinWidth(70);

        Button removeBtn = new Button("X");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + StyleManager.ACCENT
                + "; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 4 8;");
        removeBtn.setOnAction(e -> {
            cartService.removeFromCart(item.getBookId());
            dashboard.refreshCartBadge();
            refresh();
        });

        row.getChildren().addAll(info, qtySpinner, subtotal, removeBtn);
        return row;
    }

    private VBox buildSummaryPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle(StyleManager.card());

        Label title = new Label("Order Summary");
        title.setStyle(StyleManager.sectionTitle());

        Separator sep1 = new Separator();

        HBox subtotalRow = summaryRow("Subtotal", String.format("$%.2f", cartService.getSubtotal().doubleValue()));
        HBox discountRow = summaryRow("Discount (10%)*",
                String.format("-$%.2f", cartService.getDiscountAmount().doubleValue()));
        HBox taxRow = summaryRow("Tax (8%)", String.format("$%.2f", cartService.getTaxAmount().doubleValue()));

        if (cartService.getDiscountAmount().doubleValue() == 0) {
            Label hint = new Label("*Spend $50+ for 10% off");
            hint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + StyleManager.SUCCESS + "; -fx-font-style: italic;");
            panel.getChildren().add(hint);
        }

        Separator sep2 = new Separator();

        Label totalLbl = new Label("Total");
        totalLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        Label totalAmt = new Label(String.format("$%.2f", cartService.getTotal().doubleValue()));
        totalAmt.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + StyleManager.ACCENT + ";");
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        totalRow.getChildren().addAll(totalLbl, sp, totalAmt);

        Button checkoutBtn = new Button("Proceed to Checkout");
        checkoutBtn.setStyle(StyleManager.successButton());
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setPrefHeight(46);
        checkoutBtn.setDisable(cartService.isEmpty());
        checkoutBtn.setOnAction(e -> showCheckoutDialog());

        Button clearBtn = new Button("Clear Cart");
        clearBtn.setStyle(StyleManager.dangerButton());
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setDisable(cartService.isEmpty());
        clearBtn.setOnAction(e -> {
            if (AlertUtil.showConfirmation("Clear Cart", "Remove all items from your cart?")) {
                cartService.clearCart();
                dashboard.refreshCartBadge();
                refresh();
            }
        });

        panel.getChildren().addAll(title, sep1, subtotalRow, discountRow, taxRow, sep2, totalRow, checkoutBtn,
                clearBtn);
        return panel;
    }

    private HBox summaryRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        row.getChildren().addAll(lbl, sp, val);
        return row;
    }

    private void showCheckoutDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Checkout");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setPrefWidth(380);

        Label heading = new Label("Complete Your Order");
        heading.setStyle(StyleManager.sectionTitle());

        Label totalLbl = new Label(String.format("Total: $%.2f", cartService.getTotal().doubleValue()));
        totalLbl.setStyle(StyleManager.priceLabel());

        Label addrLbl = new Label("Shipping Address:");
        addrLbl.setStyle("-fx-font-weight: bold;");
        TextArea addrField = new TextArea();
        addrField.setPromptText("Enter your shipping address...");
        addrField.setPrefRowCount(3);
        addrField.setStyle("-fx-font-size: 13px;");

        Label payLbl = new Label("Payment Method (Demo):");
        payLbl.setStyle("-fx-font-weight: bold;");
        ComboBox<String> payMethod = new ComboBox<>();
        payMethod.getItems().addAll("Credit Card", "PayPal", "Bank Transfer");
        payMethod.setValue("Credit Card");
        payMethod.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().addAll(heading, totalLbl, new Separator(), addrLbl, addrField, payLbl, payMethod);
        dialog.getDialogPane().setContent(content);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Place Order");
        okBtn.setStyle(StyleManager.successButton());

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? addrField.getText() : null);

        dialog.showAndWait().ifPresent(address -> {
            if (address.isBlank()) {
                AlertUtil.showError("Validation Error", "Please enter a shipping address");
                return;
            }
            try {
                Order order = orderService.placeOrder(authService.getCurrentUser(), cartService, address);
                dashboard.refreshCartBadge();
                AlertUtil
                        .showSuccess("Order #" + order.getId() + " placed successfully!\nThank you for your purchase.");
                refresh();
            } catch (Exception ex) {
                AlertUtil.showError("Checkout Failed", ex.getMessage());
            }
        });
    }

    private void refresh() {
        buildUI();
    }

    public Parent getRoot() {
        return root;
    }

}
