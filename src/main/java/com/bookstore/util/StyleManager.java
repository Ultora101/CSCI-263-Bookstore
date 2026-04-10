package com.bookstore.util;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Centralized styling utility for consistent UI theming.
 *
 * @author BookStore Team
 * @version 1.0
 */
public class StyleManager {

    // Color palette
    public static final String PRIMARY = "#2C3E50";
    public static final String PRIMARY_LIGHT = "#34495E";
    public static final String ACCENT = "#E74C3C";
    public static final String ACCENT_HOVER = "#C0392B";
    public static final String SUCCESS = "#27AE60";
    public static final String WARNING = "#F39C12";
    public static final String INFO = "#2980B9";
    public static final String LIGHT_BG = "#ECF0F1";
    public static final String WHITE = "#FFFFFF";
    public static final String TEXT_DARK = "#2C3E50";
    public static final String TEXT_MUTED = "#7F8C8D";
    public static final String BORDER = "#BDC3C7";
    public static final String CARD_BG = "#FAFAFA";
    public static final String SIDEBAR_BG = "#2C3E50";
    public static final String ADMIN_ACCENT = "#8E44AD";

    // Button styles
    public static String primaryButton() {
        return "-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; " +
                "-fx-cursor: hand; -fx-background-radius: 6;";
    }

    public static String secondaryButton() {
        return "-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; " +
                "-fx-cursor: hand; -fx-background-radius: 6;";
    }

    public static String successButton() {
        return "-fx-background-color: " + SUCCESS + "; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; " +
                "-fx-cursor: hand; -fx-background-radius: 6;";
    }

    public static String dangerButton() {
        return "-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 6 14; " +
                "-fx-cursor: hand; -fx-background-radius: 5;";
    }

    public static String outlineButton() {
        return "-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; " +
                "-fx-border-color: " + PRIMARY + "; -fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-font-size: 13px; -fx-padding: 9 19; -fx-cursor: hand;";
    }

    public static String smallInfoButton() {
        return "-fx-background-color: " + INFO + "; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-padding: 5 12; " +
                "-fx-cursor: hand; -fx-background-radius: 4;";
    }

    // Card
    public static String card() {
        return "-fx-background-color: " + WHITE + "; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);";
    }

    // Input
    public static String textField() {
        return "-fx-background-color: white; -fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-padding: 8 12; -fx-font-size: 13px;";
    }

    // Labels
    public static String pageTitle() {
        return "-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_DARK + ";";
    }

    public static String sectionTitle() {
        return "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_DARK + ";";
    }

    public static String mutedLabel() {
        return "-fx-font-size: 12px; -fx-text-fill: " + TEXT_MUTED + ";";
    }

    public static String priceLabel() {
        return "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT + ";";
    }

    public static String badgeSuccess() {
        return "-fx-background-color: #D5F5E3; -fx-text-fill: #1E8449; " +
                "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;";
    }

    public static String badgeWarning() {
        return "-fx-background-color: #FDEBD0; -fx-text-fill: #9C640C; " +
                "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;";
    }

    public static String badgeDanger() {
        return "-fx-background-color: #FADBD8; -fx-text-fill: #922B21; " +
                "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;";
    }

    public static String badgeInfo() {
        return "-fx-background-color: #D6EAF8; -fx-text-fill: #1A5276; " +
                "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;";
    }

    // Sidebar nav button
    public static String navButton(boolean active) {
        if (active) {
            return "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 20; " +
                    "-fx-cursor: hand; -fx-alignment: CENTER-LEFT; -fx-background-radius: 8; " +
                    "-fx-border-color: transparent transparent transparent #E74C3C; -fx-border-width: 0 0 0 4;";
        }
        return "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); " +
                "-fx-font-size: 14px; -fx-padding: 12 20; " +
                "-fx-cursor: hand; -fx-alignment: CENTER-LEFT; -fx-background-radius: 8;";
    }

    public static void styleTableView(TableView<?> table) {
        table.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
    }
}
