package com.expensetracker;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TransactionsController implements Initializable {

    @FXML private TextField       searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private VBox            transactionsContainer;
    @FXML private Label           emptyLabel;
    @FXML private Label           summaryLabel;

    private final TransactionStore store = TransactionStore.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate category filter from live data
        categoryFilter.getItems().add("All Categories");
        store.getAll().stream()
             .map(Transaction::getCategory)
             .distinct()
             .sorted()
             .forEach(categoryFilter.getItems()::add);
        categoryFilter.getSelectionModel().selectFirst();

        typeFilter.getItems().addAll("All Types", "Income", "Expense");
        typeFilter.getSelectionModel().selectFirst();

        renderTransactions(store.getAll());
    }

    // ── Render ────────────────────────────────────────────────────────────────

    private void renderTransactions(List<Transaction> list) {
        transactionsContainer.getChildren().clear();

        if (list.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        } else {
            emptyLabel.setVisible(false);
            emptyLabel.setManaged(false);
            // Newest first
            for (int i = list.size() - 1; i >= 0; i--) {
                transactionsContainer.getChildren().add(buildRow(list.get(i)));
            }
        }

        double income   = list.stream().filter(Transaction::isIncome).mapToDouble(Transaction::getAmount).sum();
        double expenses = list.stream().filter(t -> !t.isIncome()).mapToDouble(Transaction::getAmount).sum();
        summaryLabel.setText(list.size() + " transactions  |  " +
                "In: +Rs " + fmt(income) + "  Out: -Rs " + fmt(expenses));
    }

    private HBox buildRow(Transaction t) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 12 16 12 16; " +
                     "-fx-background-color: rgba(255,255,255,0.02); " +
                     "-fx-background-radius: 8;");
        row.setOnMouseEntered(e -> row.setStyle(row.getStyle()
                .replace("rgba(255,255,255,0.02)", "rgba(255,255,255,0.05)")));
        row.setOnMouseExited(e -> row.setStyle(row.getStyle()
                .replace("rgba(255,255,255,0.05)", "rgba(255,255,255,0.02)")));

        // Title + icon column
        HBox titleCol = new HBox(10);
        titleCol.setAlignment(Pos.CENTER_LEFT);
        titleCol.setPrefWidth(230);
        String icon = categoryIcon(t.getCategory());
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        Label titleLabel = new Label(t.getTitle());
        titleLabel.setStyle("-fx-text-fill: #e8f5f0; -fx-font-family: 'Trebuchet MS';" +
                            " -fx-font-size: 13px; -fx-font-weight: bold;");
        titleLabel.setMaxWidth(180);
        titleLabel.setEllipsisString("…");
        titleCol.getChildren().addAll(iconLabel, titleLabel);

        // Category pill
        Label catLabel = new Label(t.getCategory());
        catLabel.setPrefWidth(160);
        catLabel.setStyle("-fx-text-fill: #8ca59b; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 12px;");

        // Date
        Label dateLabel = new Label(t.getDate());
        dateLabel.setPrefWidth(130);
        dateLabel.setStyle("-fx-text-fill: #8ca59b; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 12px;");

        // Type badge
        Label typeLabel = new Label(t.isIncome() ? "Income" : "Expense");
        typeLabel.setPrefWidth(100);
        String badgeStyle = t.isIncome()
                ? "-fx-background-color: rgba(0,230,118,0.12); -fx-text-fill: #00e676;"
                : "-fx-background-color: rgba(255,83,112,0.12); -fx-text-fill: #ff5370;";
        typeLabel.setStyle(badgeStyle +
                " -fx-background-radius: 6; -fx-padding: 3 10 3 10;" +
                " -fx-font-family: 'Trebuchet MS'; -fx-font-size: 11px; -fx-font-weight: bold;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Amount
        String amtTxt = t.isIncome() ? "+Rs " + fmt(t.getAmount()) : "-Rs " + fmt(t.getAmount());
        Label amtLabel = new Label(amtTxt);
        amtLabel.setPrefWidth(120);
        amtLabel.setAlignment(Pos.CENTER_RIGHT);
        amtLabel.setStyle("-fx-text-fill: " + (t.isIncome() ? "#00e676" : "#ff5370") + ";" +
                          " -fx-font-family: 'Trebuchet MS'; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Delete button
        Button del = new Button("✕");
        del.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,83,112,0.4);" +
                     " -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0 0 0 12;");
        del.setOnMouseEntered(e -> del.setStyle(del.getStyle().replace("0.4)", "0.9)")));
        del.setOnMouseExited(e -> del.setStyle(del.getStyle().replace("0.9)", "0.4)")));
        del.setOnAction(e -> {
            store.remove(t);
            applyFilters();
        });

        row.getChildren().addAll(titleCol, catLabel, dateLabel, typeLabel, spacer, amtLabel, del);
        return row;
    }

    // ── Search & Filter ───────────────────────────────────────────────────────

    @FXML private void handleSearch() { applyFilters(); }
    @FXML private void handleFilter() { applyFilters(); }

    private void applyFilters() {
        String query    = searchField.getText().trim().toLowerCase();
        String catSel   = categoryFilter.getValue();
        String typeSel  = typeFilter.getValue();

        List<Transaction> filtered = store.getAll().stream()
            .filter(t -> query.isEmpty()
                    || t.getTitle().toLowerCase().contains(query)
                    || t.getCategory().toLowerCase().contains(query))
            .filter(t -> catSel == null || catSel.equals("All Categories")
                    || t.getCategory().equals(catSel))
            .filter(t -> typeSel == null || typeSel.equals("All Types")
                    || (typeSel.equals("Income") && t.isIncome())
                    || (typeSel.equals("Expense") && !t.isIncome()))
            .collect(Collectors.toList());

        renderTransactions(filtered);
    }

    // ── Add Transaction Dialog ────────────────────────────────────────────────

    @FXML
    private void handleAddTransaction() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Transaction");
        dialog.setResizable(false);

        VBox form = new VBox(16);
        form.setStyle("-fx-background-color: #0d1520; -fx-padding: 30;");
        form.setPrefWidth(400);

        Label heading = new Label("New Transaction");
        heading.setStyle("-fx-text-fill: #ffffff; -fx-font-family: Georgia; -fx-font-size: 20px; -fx-font-weight: bold;");

        TextField titleField = styledField("Title");
        TextField amountField = styledField("Amount (e.g. 1500)");
        TextField dateField = styledField("Date (DD MMM YYYY, e.g. 12 May 2026)");

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Food & Dining", "Utilities", "Health", "Transport",
                                  "Shopping", "Education", "Entertainment", "Income", "Other");
        catBox.setPromptText("Category");
        catBox.setMaxWidth(Double.MAX_VALUE);
        catBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;" +
                        "-fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 10;" +
                        "-fx-text-fill: #e8f5f0; -fx-prompt-text-fill: rgba(140,165,155,0.5);" +
                        "-fx-pref-height: 46;");

        ToggleGroup tg = new ToggleGroup();
        RadioButton incomeRb  = new RadioButton("Income");
        RadioButton expenseRb = new RadioButton("Expense");
        incomeRb.setToggleGroup(tg); expenseRb.setToggleGroup(tg);
        expenseRb.setSelected(true);
        incomeRb.setStyle("-fx-text-fill: #00e676;"); expenseRb.setStyle("-fx-text-fill: #ff5370;");
        HBox typeRow = new HBox(20, incomeRb, expenseRb);

        Label errLabel = new Label();
        errLabel.setStyle("-fx-text-fill: #ff5370; -fx-font-size: 12px;");
        errLabel.setVisible(false);

        Button saveBtn = new Button("Add Transaction");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #00e676; -fx-text-fill: #0d0f14;" +
                         "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String ttl = titleField.getText().trim();
            String amt = amountField.getText().trim();
            String cat = catBox.getValue();
            String dt  = dateField.getText().trim();

            if (ttl.isEmpty() || amt.isEmpty() || cat == null || dt.isEmpty()) {
                errLabel.setText("Please fill in all fields.");
                errLabel.setVisible(true);
                return;
            }
            double amount;
            try { amount = Double.parseDouble(amt); }
            catch (NumberFormatException ex) {
                errLabel.setText("Amount must be a number.");
                errLabel.setVisible(true);
                return;
            }
            boolean isInc = incomeRb.isSelected();
            store.add(new Transaction(ttl, cat, amount, dt, isInc));
            // Refresh category filter options
            if (!categoryFilter.getItems().contains(cat))
                categoryFilter.getItems().add(cat);
            applyFilters();
            dialog.close();
        });

        form.getChildren().addAll(heading, titleField, amountField, catBox, dateField, typeRow, errLabel, saveBtn);
        dialog.setScene(new Scene(form));
        dialog.showAndWait();
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleNavDashboard()   { navigateTo("dashboard"); }
    @FXML private void handleNavCategories()  { navigateTo("categories"); }
    @FXML private void handleNavAnalytics()   { navigateTo("analytics"); }
    @FXML private void handleLogout()         { navigateTo("login"); }

    private void navigateTo(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private String fmt(double v) { return String.format("%,.0f", v); }

    private TextField styledField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;" +
                   "-fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 10;" +
                   "-fx-text-fill: #e8f5f0; -fx-prompt-text-fill: rgba(140,165,155,0.5);" +
                   "-fx-padding: 12 16 12 16; -fx-pref-height: 46;");
        return f;
    }

    private String categoryIcon(String category) {
        return switch (category) {
            case "Food & Dining"  -> "🍽";
            case "Utilities"      -> "💡";
            case "Health"         -> "🏥";
            case "Transport"      -> "🚗";
            case "Shopping"       -> "🛍";
            case "Education"      -> "📚";
            case "Entertainment"  -> "🎬";
            case "Income"         -> "💰";
            default               -> "📌";
        };
    }
}