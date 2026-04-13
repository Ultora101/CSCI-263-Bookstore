package com.bookstore.ui.customer;

import com.bookstore.exception.InsufficientStockException;
import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import com.bookstore.service.CartService;
import com.bookstore.ui.CustomerDashboard;
import com.bookstore.util.AlertUtil;
import com.bookstore.util.StyleManager;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.util.List;

/**
 * Book catalog view for browsing and searching books.
 *
 * @author Lyle Voth
 * @version 1.0
 */
public class CatalogView {

    private final BookService bookService;
    private final CartService cartService;
    private final CustomerDashboard dashboard;
    private VBox root;
    private FlowPane bookGrid;
    private TextField searchField;
    private ComboBox<String> genreFilter;
    private ComboBox<String> searchByBox;
    private Label resultLabel;

    public CatalogView(BookService bookService, CartService cartService, CustomerDashboard dashboard) {
        this.bookService = bookService;
        this.cartService = cartService;
        this.dashboard = dashboard;
        buildUI();
    }

    private void buildUI() {
        root = new VBox();
        root.setStyle("-fx-background-color: " + StyleManager.LIGHT_BG);

        // top bar
        HBox topBar = buildTopBar();

        // search/filter bar
        HBox filterBar = buildFilterBar();

        // results label
        resultLabel = new Label();
        resultLabel.setStyle(StyleManager.mutedLabel() + "-fx-padding: 0 0 0 24;");

        // book grid
        bookGrid = new FlowPane(16, 16);
        bookGrid.setPadding(new Insets(16, 24, 24, 24));

        ScrollPane scrollPane = new ScrollPane(bookGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-backgrond: " + StyleManager.LIGHT_BG + ";");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(topBar, filterBar, resultLabel, scrollPane);
        loadBooks(bookService.getAllBooks());
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(24, 24, 16, 24));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent " + StyleManager.BORDER
                + " transparent;");

        Label title = new Label("Book Catalog");
        title.setStyle(StyleManager.pageTitle());

        bar.getChildren().add(title);
        return bar;
    }

    private HBox buildFilterBar() {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(14, 24, 14, 24));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent " + StyleManager.BORDER
                + " transparent;");

        searchByBox = new ComboBox<>(FXCollections.observableArrayList("All Fields", "Title", "Author", "Genre"));
        searchByBox.setValue("All Fields");
        searchByBox.setStyle("-fx-font-size: 13px;");
        searchByBox.setPrefHeight(38);

        searchField = new TextField();
        searchField.setPromptText("Search books...");
        searchField.setStyle(StyleManager.textField());
        searchField.setPrefHeight(38);
        searchField.setPrefWidth(280);

        Button searchBtn = new Button("Search");
        searchBtn.setStyle(StyleManager.secondaryButton());
        searchBtn.setPrefHeight(38);

        List<String> genres = bookService.getAllGenres();
        genres.add(0, "All Genres");
        genreFilter = new ComboBox<>(FXCollections.observableArrayList(genres));
        genreFilter.setValue("All Genres");
        genreFilter.setStyle("-fx-font-size: 13px;");
        genreFilter.setPrefHeight(38);

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle(StyleManager.outlineButton() + " -fx-padding: 8 14;");
        clearBtn.setPrefHeight(38);

        searchBtn.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch());
        genreFilter.setOnAction(e -> performSearch());
        clearBtn.setOnAction(e -> {
            searchField.clear();
            genreFilter.setValue("All Genres");
            loadBooks(bookService.getAllBooks());
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label totalLabel = new Label();
        bar.getChildren().addAll(searchByBox, searchField, searchBtn, genreFilter, clearBtn);
        return bar;
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        String genre = genreFilter.getValue();
        String field = searchByBox.getValue();

        List<Book> books;
        if (!"All Genres".equals(genre)) {
            books = bookService.searchByField("genre", genre);
            if (!query.isEmpty()) {
                books = books.stream()
                        .filter(b -> b.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                                b.getAuthor().toLowerCase().contains(query.toLowerCase()))
                        .toList();
            }
        } else if (!query.isEmpty()) {
            books = "All Fields".equals(field)
                    ? bookService.searchBooks(query)
                    : bookService.searchByField(field.toLowerCase(), query);
        } else {
            books = bookService.getAllBooks();
        }
        loadBooks(books);
    }

    private void loadBooks(List<Book> books) {
        bookGrid.getChildren().clear();
        resultLabel.setText(books.size() + " book" + (books.size() != 1 ? "s" : "") + " found");
        for (Book book : books) {
            bookGrid.getChildren().add(buildBookCard(book));
        }
    }

    private VBox buildBookCard(Book book) {
        VBox card = new VBox(8);
        card.setPrefWidth(200);
        card.setPadding(new Insets(16));
        card.setStyle(StyleManager.card());

        // Book cover placeholder
        StackPane cover = new StackPane();
        cover.setPrefHeight(120);
        cover.setStyle("-fx-background-color: " + getGenreColor(book.getGenre()) + "; -fx-background-radius: 8;");
        Label coverIcon = new Label(book.getGenre().substring(0, 1).toUpperCase());
        coverIcon.setStyle("-fx-font-size: 40px;");
        cover.getChildren().add(coverIcon);

        // Availability badge
        Label availBadge = new Label(book.isAvailable() ? "In Stock" : "Out of Stock");
        availBadge.setStyle(book.isAvailable() ? StyleManager.badgeSuccess() : StyleManager.badgeDanger());

        Label titleLbl = new Label(book.getTitle());
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + StyleManager.TEXT_DARK
                + "; -fx-wrap-text: true;");
        titleLbl.setMaxWidth(180);

        Label authorLbl = new Label("by " + book.getAuthor());
        authorLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");

        Label genreLbl = new Label(book.getGenre());
        genreLbl.setStyle(StyleManager.badgeInfo());

        Label priceLbl = new Label(String.format("$%.2f", book.getPrice().doubleValue()));
        priceLbl.setStyle(StyleManager.priceLabel());

        Label stockLbl = new Label("Stock: " + book.getStockQuantity());
        stockLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");

        HBox actionRow = new HBox(6);
        actionRow.setAlignment(Pos.CENTER);

        Button detailBtn = new Button("Details");
        detailBtn.setStyle(StyleManager.smallInfoButton());

        Button addBtn = new Button("Add");
        addBtn.setStyle(StyleManager.dangerButton() + " -fx-font-size: 11px;");
        addBtn.setDisable(!book.isAvailable());

        addBtn.setOnAction(e -> addToCart(book));
        detailBtn.setOnAction(e -> showBookDetails(book));

        actionRow.getChildren().addAll(detailBtn, addBtn);

        card.getChildren().addAll(cover, availBadge, titleLbl, authorLbl, genreLbl, priceLbl, stockLbl, actionRow);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(StyleManager.card() +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 16, 0, 0, 4); -fx-translate-y: -2;"));
        card.setOnMouseExited(e -> card.setStyle(StyleManager.card()));

        return card;
    }

    private void addToCart(Book book) {
        try {
            cartService.addToCart(book, 1);
            dashboard.refreshCartBadge();
            AlertUtil.showSuccess("'" + book.getTitle() + "' added to cart!");
        } catch (InsufficientStockException ex) {
            AlertUtil.showError("Out of Stock", ex.getMessage());
        }
    }

    private void showBookDetails(Book book) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Book Details");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setPrefWidth(420);

        // Cover
        StackPane cover = new StackPane();
        cover.setPrefHeight(100);
        cover.setStyle("-fx-background-color: " + getGenreColor(book.getGenre()) + "; -fx-background-radius: 8;");
        Label coverIcon = new Label(book.getGenre().substring(0, 1).toUpperCase());
        coverIcon.setStyle("-fx-font-size: 48px;");
        cover.getChildren().add(coverIcon);

        Label titleLbl = new Label(book.getTitle());
        titleLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        titleLbl.setWrapText(true);

        GridPane info = new GridPane();
        info.setHgap(12);
        info.setVgap(8);
        addInfoRow(info, 0, "Author", book.getAuthor());
        addInfoRow(info, 1, "Genre", book.getGenre());
        addInfoRow(info, 2, "ISBN", book.getIsbn() != null ? book.getIsbn() : "N/A");
        addInfoRow(info, 3, "Publisher", book.getPublisher() != null ? book.getPublisher() : "N/A");
        addInfoRow(info, 4, "Year", String.valueOf(book.getPublishYear()));
        addInfoRow(info, 5, "Price", String.format("$%.2f", book.getPrice().doubleValue()));
        addInfoRow(info, 6, "Stock", book.getStockQuantity() + " copies");

        Label descTitle = new Label("Description");
        descTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label descLbl = new Label(book.getDescription() != null ? book.getDescription() : "No description available.");
        descLbl.setWrapText(true);
        descLbl.setStyle("-fx-text-fill: " + StyleManager.TEXT_MUTED + ";");

        Button addBtn = new Button("Add to Cart");
        addBtn.setStyle(StyleManager.primaryButton());
        addBtn.setDisable(!book.isAvailable());
        addBtn.setOnAction(e -> {
            addToCart(book);
            dialog.close();
        });

        content.getChildren().addAll(cover, titleLbl, info, new Separator(), descTitle, descLbl, addBtn);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");
        Label val = new Label(value);
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private String getGenreColor(String genre) {
        return switch (genre.toLowerCase()) {
            case "fantasy" -> "#E8F5E9";
            case "science fiction" -> "#E3F2FD";
            case "thriller" -> "#FCE4EC";
            case "romance" -> "#FFF3E0";
            case "non-fiction / history", "non-fiction" -> "#F3E5F5";
            case "self-help" -> "#E0F7FA";
            case "memoir" -> "#FFF8E1";
            default -> "#ECEFF1";
        };
    }

    public Parent getRoot() {
        return root;
    }

}
