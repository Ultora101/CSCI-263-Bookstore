package com.bookstore.ui.customer;

import com.bookstore.model.User;
import com.bookstore.service.AuthService;
import com.bookstore.util.AlertUtil;
import com.bookstore.util.StyleManager;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Customer profile view.
 */
public class ProfileView {

    private final AuthService authService;
    private VBox root;

    public ProfileView(AuthService authService) {
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
        Label title = new Label("My Profile");
        title.setStyle(StyleManager.pageTitle());
        header.getChildren().add(title);

        User user = authService.getCurrentUser();

        VBox card = new VBox(16);
        card.setPadding(new Insets(28));
        card.setMaxWidth(480);
        card.setStyle(StyleManager.card());

        StackPane avatar = new StackPane();
        avatar.setPrefSize(80, 80);
        avatar.setStyle("-fx-background-color: " + StyleManager.PRIMARY + "; -fx-background-radius: 40;");
        Label avatarIcon = new Label(user.getFullName().substring(0, 1).toUpperCase());
        avatarIcon.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");
        avatar.getChildren().add(avatarIcon);

        Label nameLbl = new Label(user.getFullName());
        nameLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        Label roleBadge = new Label(user.getRole().name());
        roleBadge.setStyle(StyleManager.badgeInfo());

        Separator sep = new Separator();

        GridPane info = new GridPane();
        info.setHgap(16);
        info.setVgap(12);
        addRow(info, 0, "Username", user.getUsername());
        addRow(info, 1, "Email", user.getEmail());
        addRow(info, 2, "Account Status", user.getStatus().name());
        addRow(info, 3, "Member Since",
                user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate().toString() : "N/A");
        addRow(info, 4, "Last Login",
                user.getLastLogin() != null ? user.getLastLogin().toLocalDate().toString() : "N/A");

        Separator sep2 = new Separator();

        Label pwTitle = new Label("Change Password");
        pwTitle.setStyle(StyleManager.sectionTitle());
        PasswordField curPw = new PasswordField();
        curPw.setPromptText("Current password");
        curPw.setStyle(StyleManager.textField());
        PasswordField newPw = new PasswordField();
        newPw.setPromptText("New password (min 6 chars)");
        newPw.setStyle(StyleManager.textField());
        PasswordField confirmPw = new PasswordField();
        confirmPw.setPromptText("Confirm new password");
        confirmPw.setStyle(StyleManager.textField());

        Button changePwBtn = new Button("Update Password");
        changePwBtn.setStyle(StyleManager.primaryButton());
        changePwBtn.setOnAction(e -> {
            String cur = curPw.getText(), nw = newPw.getText(), conf = confirmPw.getText();
            if (!nw.equals(conf)) {
                AlertUtil.showError("Error", "New passwords do not match");
                return;
            }
            try {
                // Re-authenticate with current password
                authService.login(user.getUsername(), cur);
                authService.resetPassword(user.getId(), nw);
                AlertUtil.showSuccess("Password updated successfully!");
                curPw.clear();
                newPw.clear();
                confirmPw.clear();
            } catch (Exception ex) {
                AlertUtil.showError("Error", "Current password is incorrect");
            }
        });

        card.getChildren().addAll(avatar, nameLbl, roleBadge, sep, info, sep2, pwTitle, curPw, newPw, confirmPw,
                changePwBtn);

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(24));
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        root.getChildren().addAll(header, wrapper);
    }

    private void addRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    public Parent getRoot() {
        return root;
    }
}
