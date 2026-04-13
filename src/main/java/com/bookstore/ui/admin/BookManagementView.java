package com.bookstore.ui.admin;

import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import com.bookstore.util.AlertUtil;
import com.bookstore.util.StyleManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;

/**
 * Admin view for managing the book catalog.
 */
public class BookManagementView {

    private final BookService bookService;
    private VBox root;
    private TableView<Book> table;
    private ObservableList<Book> bookList;
    private TextField searchField;

    public BookManagementView(BookService bookService) {
        this.bookService = bookService;
        buildUI();
    }

    private void buildUI() {
        root = new VBox();
        root.setStyle("-fx-background-color: " + StyleManager.LIGHT_BG + ";");

        // Header bar
        HBox header = new HBox(12);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent " + StyleManager.BORDER
                + " transparent;");

        Label title = new Label("Book Management");
        title.setStyle(StyleManager.pageTitle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search books...");
        searchField.setStyle(StyleManager.textField());
        searchField.setPrefWidth(220);
        searchField.setPrefHeight(38);
        searchField.textProperty().addListener((obs, o, n) -> filterBooks(n));

        Button addBtn = new Button("Add Book");
        addBtn.setStyle(StyleManager.successButton());
        addBtn.setPrefHeight(38);
        addBtn.setOnAction(e -> showBookDialog(null));

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(StyleManager.outlineButton() + " -fx-padding: 8 16;");
        refreshBtn.setPrefHeight(38);
        refreshBtn.setOnAction(e -> loadBooks());

        header.getChildren().addAll(title, spacer, searchField, addBtn, refreshBtn);

        // Table
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox tableWrapper = new VBox(table);
        tableWrapper.setPadding(new Insets(16, 20, 20, 20));
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);

        root.getChildren().addAll(header, tableWrapper);
        loadBooks();
    }

    private TableView<Book> buildTable() {
        TableView<Book> tv = new TableView<>();
        StyleManager.styleTableView(tv);
        tv.setPlaceholder(new Label("No books found"));

        TableColumn<Book, String> idCol = col("ID", 50, b -> String.valueOf(b.getId()));
        TableColumn<Book, String> titleCol = col("Title", 200, Book::getTitle);
        TableColumn<Book, String> authorCol = col("Author", 160, Book::getAuthor);
        TableColumn<Book, String> genreCol = col("Genre", 130, Book::getGenre);
        TableColumn<Book, String> isbnCol = col("ISBN", 140, b -> b.getIsbn() != null ? b.getIsbn() : "—");
        TableColumn<Book, String> priceCol = col("Price", 80, b -> String.format("$%.2f", b.getPrice().doubleValue()));
        TableColumn<Book, String> stockCol = col("Stock", 70, b -> String.valueOf(b.getStockQuantity()));

        TableColumn<Book, String> availCol = new TableColumn<>("Status");
        availCol.setPrefWidth(90);
        availCol.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().isAvailable() ? "In Stock" : "Out of Stock"));
        availCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setStyle("In Stock".equals(item)
                        ? "-fx-text-fill: #1E8449; -fx-font-weight: bold;"
                        : "-fx-text-fill: #922B21; -fx-font-weight: bold;");
            }
        });

        TableColumn<Book, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(160);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.setStyle(StyleManager.smallInfoButton());
                deleteBtn.setStyle(StyleManager.dangerButton() + " -fx-font-size: 11px; -fx-padding: 5 10;");
                editBtn.setOnAction(e -> {
                    Book b = getTableView().getItems().get(getIndex());
                    showBookDialog(b);
                });
                deleteBtn.setOnAction(e -> {
                    Book b = getTableView().getItems().get(getIndex());
                    handleDelete(b);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                HBox box = new HBox(6, editBtn, deleteBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        tv.getColumns().addAll(idCol, titleCol, authorCol, genreCol, isbnCol, priceCol, stockCol, availCol, actionsCol);
        return tv;
    }

    private TableColumn<Book, String> col(String name, double width,
            java.util.function.Function<Book, String> extractor) {
        TableColumn<Book, String> col = new TableColumn<>(name);
        col.setPrefWidth(width);
        col.setCellValueFactory(d -> new SimpleStringProperty(extractor.apply(d.getValue())));
        return col;
    }

    private void loadBooks() {
        bookList = FXCollections.observableArrayList(bookService.getAllBooks());
        table.setItems(bookList);
    }

    private void filterBooks(String query) {
        if (query == null || query.isBlank()) {
            loadBooks();
            return;
        }
        table.setItems(bookList.filtered(b -> b.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                b.getAuthor().toLowerCase().contains(query.toLowerCase()) ||
                b.getGenre().toLowerCase().contains(query.toLowerCase())));
    }

    private void handleDelete(Book book) {
        if (AlertUtil.showConfirmation("Delete Book",
                "Delete '" + book.getTitle() + "'? This cannot be undone.")) {
            try {
                bookService.deleteBook(book.getId());
                loadBooks();
                AlertUtil.showSuccess("Book deleted successfully.");
            } catch (Exception ex) {
                AlertUtil.showError("Delete Failed", ex.getMessage());
            }
        }
    }

    private void showBookDialog(Book existing) {
        boolean isEdit = existing != null;
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Book" : "Add New Book");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField titleF = field(isEdit ? existing.getTitle() : "", "Book Title *");
        TextField authorF = field(isEdit ? existing.getAuthor() : "", "Author *");
        TextField genreF = field(isEdit ? existing.getGenre() : "", "Genre *");
        TextField isbnF = field(isEdit && existing.getIsbn() != null ? existing.getIsbn() : "", "ISBN");
        TextField priceF = field(isEdit ? String.valueOf(existing.getPrice().doubleValue()) : "", "Price *");
        TextField stockF = field(isEdit ? String.valueOf(existing.getStockQuantity()) : "0", "Stock Quantity *");
        TextField pubF = field(isEdit && existing.getPublisher() != null ? existing.getPublisher() : "", "Publisher");
        TextField yearF = field(isEdit ? String.valueOf(existing.getPublishYear()) : "", "Publish Year");
        TextArea descF = new TextArea(isEdit && existing.getDescription() != null ? existing.getDescription() : "");
        descF.setPromptText("Description");
        descF.setPrefRowCount(3);
        descF.setStyle("-fx-font-size: 13px;");

        int r = 0;
        grid.add(lbl("Title *"), 0, r);
        grid.add(titleF, 1, r++);
        grid.add(lbl("Author *"), 0, r);
        grid.add(authorF, 1, r++);
        grid.add(lbl("Genre *"), 0, r);
        grid.add(genreF, 1, r++);
        grid.add(lbl("ISBN"), 0, r);
        grid.add(isbnF, 1, r++);
        grid.add(lbl("Price *"), 0, r);
        grid.add(priceF, 1, r++);
        grid.add(lbl("Stock *"), 0, r);
        grid.add(stockF, 1, r++);
        grid.add(lbl("Publisher"), 0, r);
        grid.add(pubF, 1, r++);
        grid.add(lbl("Year"), 0, r);
        grid.add(yearF, 1, r++);
        grid.add(lbl("Desc"), 0, r);
        grid.add(descF, 1, r++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(480);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(isEdit ? "Update" : "Add Book");

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK)
                return null;
            try {
                Book book = isEdit ? existing : new Book();
                book.setTitle(titleF.getText().trim());
                book.setAuthor(authorF.getText().trim());
                book.setGenre(genreF.getText().trim());
                book.setIsbn(isbnF.getText().trim().isEmpty() ? null : isbnF.getText().trim());
                book.setPrice(new BigDecimal(priceF.getText().trim()));
                book.setStockQuantity(Integer.parseInt(stockF.getText().trim()));
                book.setPublisher(pubF.getText().trim().isEmpty() ? null : pubF.getText().trim());
                book.setPublishYear(yearF.getText().trim().isEmpty() ? 0 : Integer.parseInt(yearF.getText().trim()));
                book.setDescription(descF.getText().trim().isEmpty() ? null : descF.getText().trim());
                return book;
            } catch (NumberFormatException ex) {
                AlertUtil.showError("Validation Error", "Price and stock must be valid numbers.");
                return null;
            }
        });

        dialog.showAndWait().ifPresent(book -> {
            try {
                if (isEdit)
                    bookService.updateBook(book);
                else
                    bookService.addBook(book);
                loadBooks();
                AlertUtil.showSuccess("Book " + (isEdit ? "updated" : "added") + " successfully!");
            } catch (Exception ex) {
                AlertUtil.showError("Error", ex.getMessage());
            }
        });
    }

    private TextField field(String val, String prompt) {
        TextField tf = new TextField(val);
        tf.setPromptText(prompt);
        tf.setStyle(StyleManager.textField());
        tf.setPrefWidth(260);
        return tf;
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + StyleManager.TEXT_MUTED + ";");
        return l;
    }

    public Parent getRoot() {
        return root;
    }
}
