package com.expensetracker;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CategoriesController implements Initializable {

    @FXML private FlowPane categoryGrid;

    @FXML private Label selectedCategoryTitle;
    @FXML private Label selectedCategoryTotal;
    @FXML private VBox  categoryTransactionsList;
    @FXML private Label noCatTransLabel;

    private final TransactionStore store = TransactionStore.getInstance();

    /** Category → accent colour mapping */
    private static final Map<String, String> CAT_COLORS = new LinkedHashMap<>();
    static {
        CAT_COLORS.put("Food & Dining",  "#ff9800");
        CAT_COLORS.put("Utilities",      "#29b6f6");
        CAT_COLORS.put("Health",         "#ef5350");
        CAT_COLORS.put("Transport",      "#ab47bc");
        CAT_COLORS.put("Shopping",       "#ec407a");
        CAT_COLORS.put("Education",      "#26c6da");
        CAT_COLORS.put("Entertainment",  "#66bb6a");
        CAT_COLORS.put("Income",         "#00e676");
        CAT_COLORS.put("Other",          "#8ca59b");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildCategoryGrid();
    }

    // ── Category Cards ────────────────────────────────────────────────────────

    private void buildCategoryGrid() {
        categoryGrid.getChildren().clear();

        Map<String, Double> expMap    = store.expensesByCategory();
        Map<String, Long>   countMap  = new LinkedHashMap<>();

        store.getAll().stream()
             .filter(t -> !t.isIncome())
             .forEach(t -> countMap.merge(t.getCategory(), 1L, Long::sum));

        // Also show Income as a card
        double totalIncome = store.totalIncome();
        long   incomeCount = store.getAll().stream().filter(Transaction::isIncome).count();

        // Expense categories first, sorted by amount
        expMap.entrySet().stream()
              .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
              .forEach(e -> categoryGrid.getChildren().add(
                  buildCategoryCard(e.getKey(), e.getValue(),
                                    countMap.getOrDefault(e.getKey(), 0L), false)));

        // Income card
        if (totalIncome > 0) {
            categoryGrid.getChildren().add(buildCategoryCard("Income", totalIncome, incomeCount, true));
        }
    }

    private VBox buildCategoryCard(String name, double total, long count, boolean isIncome) {
        String color = CAT_COLORS.getOrDefault(name, "#8ca59b");

        VBox card = new VBox(12);
        card.setPrefWidth(230);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: rgba(18,22,32,0.85);" +
                      "-fx-background-radius: 16;" +
                      "-fx-border-color: " + color + "44;" +
                      "-fx-border-radius: 16;" +
                      "-fx-border-width: 1.5;" +
                      "-fx-padding: 20;" +
                      "-fx-cursor: hand;" +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 4);");
        card.setCursor(Cursor.HAND);

        // Icon row
        HBox iconRow = new HBox(10);
        iconRow.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(categoryIcon(name));
        icon.setStyle("-fx-font-size: 22px;");
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #e8f5f0; -fx-font-family: 'Trebuchet MS';" +
                           " -fx-font-size: 14px; -fx-font-weight: bold;");
        iconRow.getChildren().addAll(icon, nameLabel);

        // Amount
        String prefix = isIncome ? "+Rs " : "-Rs ";
        Label amtLabel = new Label(prefix + fmt(total));
        amtLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-family: Georgia;" +
                          " -fx-font-size: 22px; -fx-font-weight: bold;");

        // Count
        Label cntLabel = new Label(count + (count == 1 ? " transaction" : " transactions"));
        cntLabel.setStyle("-fx-text-fill: #8ca59b; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 12px;");

        card.getChildren().addAll(iconRow, amtLabel, cntLabel);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle()
                .replace("rgba(18,22,32,0.85)", "rgba(28,34,48,0.95)")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
                .replace("rgba(28,34,48,0.95)", "rgba(18,22,32,0.85)")));

        // Click: show transactions for this category
        card.setOnMouseClicked(e -> showCategoryTransactions(name, total, isIncome, color));

        return card;
    }

    // ── Per-category Transaction Drill-Down ───────────────────────────────────

    private void showCategoryTransactions(String category, double total, boolean isIncome, String color) {
        selectedCategoryTitle.setText(categoryIcon(category) + "  " + category);
        selectedCategoryTotal.setText((isIncome ? "+Rs " : "-Rs ") + fmt(total));
        selectedCategoryTotal.setStyle("-fx-text-fill: " + color +
                "; -fx-font-family: Georgia; -fx-font-size: 22px; -fx-font-weight: bold;");

        categoryTransactionsList.getChildren().clear();
        noCatTransLabel.setVisible(false);
        noCatTransLabel.setManaged(false);

        List<Transaction> catTxns = store.getAll().stream()
                .filter(t -> t.getCategory().equals(category))
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .collect(Collectors.toList());

        for (Transaction t : catTxns) {
            categoryTransactionsList.getChildren().add(buildTxRow(t, color));
        }
    }

    private HBox buildTxRow(Transaction t, String accentColor) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10 8 10 8; -fx-background-color: rgba(255,255,255,0.02);" +
                     "-fx-background-radius: 8;");

        Label title = new Label(t.getTitle());
        title.setPrefWidth(280);
        title.setStyle("-fx-text-fill: #e8f5f0; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 13px;");

        Label date = new Label(t.getDate());
        date.setPrefWidth(160);
        date.setStyle("-fx-text-fill: #8ca59b; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String prefix = t.isIncome() ? "+Rs " : "-Rs ";
        Label amt = new Label(prefix + fmt(t.getAmount()));
        amt.setStyle("-fx-text-fill: " + accentColor + "; -fx-font-family: 'Trebuchet MS';" +
                     " -fx-font-size: 13px; -fx-font-weight: bold;");

        row.getChildren().addAll(title, date, spacer, amt);
        return row;
    }

    // ── Add Category Dialog ───────────────────────────────────────────────────

    @FXML
    private void handleAddCategory() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Category");
        dialog.setResizable(false);

        VBox form = new VBox(16);
        form.setStyle("-fx-background-color: #0d1520; -fx-padding: 30;");
        form.setPrefWidth(360);

        Label heading = new Label("New Category");
        heading.setStyle("-fx-text-fill: #ffffff; -fx-font-family: Georgia;" +
                         " -fx-font-size: 20px; -fx-font-weight: bold;");

        TextField nameField = styledField("Category name (e.g. Travel)");

        Label errLabel = new Label();
        errLabel.setStyle("-fx-text-fill: #ff5370; -fx-font-size: 12px;");
        errLabel.setVisible(false);

        Button saveBtn = new Button("Add Category");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #00e676; -fx-text-fill: #0d0f14;" +
                         "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 12; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String catName = nameField.getText().trim();
            if (catName.isEmpty()) {
                errLabel.setText("Please enter a category name.");
                errLabel.setVisible(true);
                return;
            }
            if (CAT_COLORS.containsKey(catName)) {
                errLabel.setText("Category already exists.");
                errLabel.setVisible(true);
                return;
            }
            CAT_COLORS.put(catName, "#8ca59b");
            buildCategoryGrid();
            dialog.close();
        });

        form.getChildren().addAll(heading, nameField, errLabel, saveBtn);
        dialog.setScene(new Scene(form));
        dialog.showAndWait();
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleNavDashboard()    { navigateTo("dashboard"); }
    @FXML private void handleNavTransactions() { navigateTo("transactions"); }
    @FXML private void handleNavAnalytics()    { navigateTo("analytics"); }
    @FXML private void handleLogout()          { navigateTo("login"); }

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