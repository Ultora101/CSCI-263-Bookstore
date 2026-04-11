package com.bookstore.ui.admin;

import com.bookstore.model.User;
import com.bookstore.service.AuthService;
import com.bookstore.service.UserService;
import com.bookstore.util.AlertUtil;
import com.bookstore.util.StyleManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Admin view for managing user accounts.
 */
public class UserManagementView {

    private final UserService userService;
    private final AuthService authService;
    private VBox root;
    private TableView<User> table;
    private ObservableList<User> userList;

    public UserManagementView(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
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
        Label title = new Label("User Management");
        title.setStyle(StyleManager.pageTitle());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(StyleManager.outlineButton() + " -fx-padding: 8 16;");
        refreshBtn.setOnAction(e -> loadUsers());
        header.getChildren().addAll(title, spacer, refreshBtn);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox tableWrapper = new VBox(table);
        tableWrapper.setPadding(new Insets(16, 20, 20, 20));
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);

        root.getChildren().addAll(header, tableWrapper);
        loadUsers();
    }

    private TableView<User> buildTable() {
        TableView<User> tv = new TableView<>();
        StyleManager.styleTableView(tv);

        TableColumn<User, String> idCol = col("ID", 50, u -> String.valueOf(u.getId()));
        TableColumn<User, String> userCol = col("Username", 130, User::getUsername);
        TableColumn<User, String> nameCol = col("Full Name", 160, User::getFullName);
        TableColumn<User, String> emailCol = col("Email", 200, User::getEmail);
        TableColumn<User, String> joinedCol = col("Joined", 110,
                u -> u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : "N/A");
        TableColumn<User, String> lastLoginCol = col("Last Login", 110,
                u -> u.getLastLogin() != null ? u.getLastLogin().toLocalDate().toString() : "Never");

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setPrefWidth(90);
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().name()));
        roleCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setStyle("ADMIN".equals(item)
                        ? "-fx-text-fill: #6C3483; -fx-font-weight: bold;"
                        : "-fx-text-fill: #1A5276; -fx-font-weight: bold;");
            }
        });

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(90);
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
                setStyle("ACTIVE".equals(item)
                        ? "-fx-text-fill: #1E8449; -fx-font-weight: bold;"
                        : "-fx-text-fill: #922B21; -fx-font-weight: bold;");
            }
        });

        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(260);
        actionsCol.setCellFactory(c -> new TableCell<>() {
            private final Button blockBtn = new Button();
            private final Button resetBtn = new Button("Reset PW");
            {
                resetBtn.setStyle("-fx-background-color: " + StyleManager.WARNING + "; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-padding: 5 10; -fx-cursor: hand; -fx-background-radius: 4;");
                blockBtn.setStyle(StyleManager.dangerButton() + " -fx-font-size: 11px; -fx-padding: 5 10;");
                blockBtn.setOnAction(e -> handleBlockToggle(getTableView().getItems().get(getIndex())));
                resetBtn.setOnAction(e -> handlePasswordReset(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                User user = getTableView().getItems().get(getIndex());
                boolean isSelf = user.getId() == authService.getCurrentUser().getId();
                blockBtn.setText(user.getStatus() == User.Status.ACTIVE ? "Block" : "Unblock");
                blockBtn.setDisable(isSelf || user.isAdmin());
                resetBtn.setDisable(isSelf);
                HBox box = new HBox(6, blockBtn, resetBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        tv.getColumns().addAll(idCol, userCol, nameCol, emailCol, roleCol, statusCol, joinedCol, lastLoginCol,
                actionsCol);
        return tv;
    }

    private void handleBlockToggle(User user) {
        boolean isBlocked = user.getStatus() == User.Status.BLOCKED;
        String action = isBlocked ? "unblock" : "block";
        if (AlertUtil.showConfirmation("Confirm",
                "Are you sure you want to " + action + " user '" + user.getUsername() + "'?")) {
            try {
                if (isBlocked)
                    userService.unblockUser(user.getId());
                else
                    userService.blockUser(user.getId());
                loadUsers();
                AlertUtil.showSuccess("User " + (isBlocked ? "unblocked" : "blocked") + " successfully.");
            } catch (Exception ex) {
                AlertUtil.showError("Error", ex.getMessage());
            }
        }
    }

    private void handlePasswordReset(User user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + user.getUsername());
        dialog.setContentText("New Password:");
        dialog.showAndWait().ifPresent(newPw -> {
            if (newPw.isBlank() || newPw.length() < 6) {
                AlertUtil.showError("Validation Error", "Password must be at least 6 characters");
                return;
            }
            try {
                authService.resetPassword(user.getId(), newPw);
                AlertUtil.showSuccess("Password reset successfully for " + user.getUsername());
            } catch (Exception ex) {
                AlertUtil.showError("Error", ex.getMessage());
            }
        });
    }

    private TableColumn<User, String> col(String name, double width, java.util.function.Function<User, String> fn) {
        TableColumn<User, String> col = new TableColumn<>(name);
        col.setPrefWidth(width);
        col.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return col;
    }

    private void loadUsers() {
        userList = FXCollections.observableArrayList(userService.getAllUsers());
        table.setItems(userList);
    }

    public Parent getRoot() {
        return root;
    }
}
