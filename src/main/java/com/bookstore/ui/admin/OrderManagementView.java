package com.bookstore.ui.admin;

import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.service.OrderService;
import com.bookstore.util.AlertUtil;
import com.bookstore.util.StyleManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Admin view for managing orders and updating statuses.
 */
public class OrderManagementView {

    private final OrderService orderService;
    private VBox root;
    private TableView<Order> table;
    private ObservableList<Order> orderList;

    public OrderManagementView(OrderService orderService) {
        this.orderService = orderService;
        buildUI();
    }

    private void buildUI() {
        root = new VBox();
        root.setStyle("-fx-background-color: " + StyleManager.LIGHT_BG + ";");

        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent " + StyleManager.BORDER
                + " transparent;");
        Label title = new Label("Order Management");
        title.setStyle(StyleManager.pageTitle());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setStyle(StyleManager.outlineButton() + " -fx-padding: 8 16;");
        refreshBtn.setOnAction(e -> loadOrders());
        header.getChildren().addAll(title, spacer, refreshBtn);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox tableWrapper = new VBox(table);
        tableWrapper.setPadding(new Insets(16, 20, 20, 20));
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);

        root.getChildren().addAll(header, tableWrapper);
        loadOrders();
    }

    private TableView<Order> buildTable() {
        TableView<Order> tv = new TableView<>();
        StyleManager.styleTableView(tv);

        TableColumn<Order, String> idCol = col("Order #", 70, o -> "#" + o.getId());
        TableColumn<Order, String> userCol = col("Customer", 130, Order::getUsername);
        TableColumn<Order, String> itemsCol = col("Items", 60, o -> String.valueOf(o.getItems().size()));
        TableColumn<Order, String> subCol = col("Subtotal", 90,
                o -> String.format("$%.2f", o.getSubtotal().doubleValue()));
        TableColumn<Order, String> discCol = col("Discount", 90,
                o -> String.format("-$%.2f", o.getDiscountAmount().doubleValue()));
        TableColumn<Order, String> taxCol = col("Tax", 80, o -> String.format("$%.2f", o.getTaxAmount().doubleValue()));
        TableColumn<Order, String> totalCol = col("Total", 90,
                o -> String.format("$%.2f", o.getTotalAmount().doubleValue()));
        TableColumn<Order, String> dateCol = col("Date", 110,
                o -> o.getCreatedAt() != null ? o.getCreatedAt().toLocalDate().toString() : "N/A");

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(110);
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setStyle(switch (item) {
                    case "DELIVERED" -> "-fx-text-fill: #1E8449; -fx-font-weight: bold;";
                    case "SHIPPED" -> "-fx-text-fill: #9C640C; -fx-font-weight: bold;";
                    case "CANCELLED" -> "-fx-text-fill: #922B21; -fx-font-weight: bold;";
                    case "CONFIRMED" -> "-fx-text-fill: #1A5276; -fx-font-weight: bold;";
                    default -> "-fx-text-fill: #6C3483; -fx-font-weight: bold;";
                });
            }
        });

        TableColumn<Order, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(c -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button updateBtn = new Button("Status");
            {
                viewBtn.setStyle(StyleManager.smallInfoButton());
                updateBtn.setStyle("-fx-background-color: " + StyleManager.WARNING + "; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 4;");
                viewBtn.setOnAction(e -> showOrderDetails(getTableView().getItems().get(getIndex())));
                updateBtn.setOnAction(e -> showStatusDialog(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                HBox box = new HBox(6, viewBtn, updateBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        tv.getColumns().addAll(idCol, userCol, itemsCol, subCol, discCol, taxCol, totalCol, statusCol, dateCol,
                actionsCol);
        return tv;
    }

    private void loadOrders() {
        orderList = FXCollections.observableArrayList(orderService.getAllOrders());
        table.setItems(orderList);
    }

    private void showOrderDetails(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order #" + order.getId() + " Details");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setPrefWidth(460);

        Label heading = new Label("Order #" + order.getId());
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane meta = new GridPane();
        meta.setHgap(16);
        meta.setVgap(8);
        addRow(meta, 0, "Customer", order.getUsername());
        addRow(meta, 1, "Status", order.getStatus().name());
        addRow(meta, 2, "Date", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "N/A");
        addRow(meta, 3, "Address", order.getShippingAddress() != null ? order.getShippingAddress() : "N/A");

        Label itemsTitle = new Label("Items:");
        itemsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox itemsList = new VBox(6);
        for (OrderItem item : order.getItems()) {
            HBox row = new HBox(12);
            Label name = new Label(item.getBookTitle() + " by " + item.getBookAuthor());
            name.setStyle("-fx-font-size: 13px;");
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label qty = new Label("x" + item.getQuantity());
            qty.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");
            Label subtotal = new Label(String.format("$%.2f", item.getSubtotal().doubleValue()));
            subtotal.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
            row.getChildren().addAll(name, sp, qty, subtotal);
            itemsList.getChildren().add(row);
        }

        Separator sep = new Separator();

        GridPane totals = new GridPane();
        totals.setHgap(16);
        totals.setVgap(6);
        addRow(totals, 0, "Subtotal", String.format("$%.2f", order.getSubtotal().doubleValue()));
        addRow(totals, 1, "Discount", String.format("-$%.2f", order.getDiscountAmount().doubleValue()));
        addRow(totals, 2, "Tax", String.format("$%.2f", order.getTaxAmount().doubleValue()));
        addRow(totals, 3, "Total", String.format("$%.2f", order.getTotalAmount().doubleValue()));

        content.getChildren().addAll(heading, new Separator(), meta, itemsTitle, itemsList, sep, totals);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showStatusDialog(Order order) {
        ChoiceDialog<Order.Status> dialog = new ChoiceDialog<>(order.getStatus(), Order.Status.values());
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Order #" + order.getId());
        dialog.setContentText("New Status:");
        dialog.showAndWait().ifPresent(newStatus -> {
            orderService.updateOrderStatus(order.getId(), newStatus);
            loadOrders();
            AlertUtil.showSuccess("Order #" + order.getId() + " status updated to " + newStatus);
        });
    }

    private TableColumn<Order, String> col(String name, double width, java.util.function.Function<Order, String> fn) {
        TableColumn<Order, String> col = new TableColumn<>(name);
        col.setPrefWidth(width);
        col.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return col;
    }

    private void addRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");
        grid.add(lbl, 0, row);
        grid.add(new Label(value), 1, row);
    }

    public Parent getRoot() {
        return root;
    }
}
