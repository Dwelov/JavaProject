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
        new Thread(() -> {
            store.fetchTransactions();
            javafx.application.Platform.runLater(() -> {
                buildCategoryGrid();
            });
        }).start();
    }

    // ── Category Cards ────────────────────────────────────────────────────────

    private void buildCategoryGrid() {
        categoryGrid.getChildren().clear();

        Map<String, Double> expMap    = store.expensesByCategory();
        Map<String, Long>   countMap  = new LinkedHashMap<>();

        store.getAll().stream()
             .filter(t -> !t.isIncome())
             .forEach(t -> countMap.merge(t.getCategory(), 1L, Long::sum));

        // Get all categories from CAT_COLORS to ensure they all show up
        List<String> allExpenseCats = new ArrayList<>(CAT_COLORS.keySet());
        allExpenseCats.remove("Income"); // Handled separately

        // Sort: those with expenses first (by amount), then the rest alphabetically
        allExpenseCats.sort((a, b) -> {
            double amtA = expMap.getOrDefault(a, 0.0);
            double amtB = expMap.getOrDefault(b, 0.0);
            if (amtA != amtB) return Double.compare(amtB, amtA);
            return a.compareTo(b);
        });

        for (String cat : allExpenseCats) {
            categoryGrid.getChildren().add(
                buildCategoryCard(cat, expMap.getOrDefault(cat, 0.0),
                                  countMap.getOrDefault(cat, 0L), false));
        }

        // Income card
        double totalIncome = store.totalIncome();
        long   incomeCount = store.getAll().stream().filter(Transaction::isIncome).count();
        categoryGrid.getChildren().add(buildCategoryCard("Income", totalIncome, incomeCount, true));
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

        // Icon row + Delete Button
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label(categoryIcon(name));
        icon.setStyle("-fx-font-size: 22px;");
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #e8f5f0; -fx-font-family: 'Trebuchet MS';" +
                           " -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,83,112,0.5); -fx-padding: 0; -fx-font-size: 14px;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5370; -fx-padding: 0; -fx-font-size: 14px;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,83,112,0.5); -fx-padding: 0; -fx-font-size: 14px;"));
        
        deleteBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            handleDeleteCategory(name);
        });
        
        topRow.getChildren().addAll(icon, nameLabel, spacer);
        if (!isIncome && !name.equals("Other")) {
            topRow.getChildren().add(deleteBtn);
        }

        // Amount
        String prefix = isIncome ? "+Rs " : "-Rs ";
        Label amtLabel = new Label(prefix + fmt(total));
        amtLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-family: Georgia;" +
                          " -fx-font-size: 22px; -fx-font-weight: bold;");

        // Count
        Label cntLabel = new Label(count + (count == 1 ? " transaction" : " transactions"));
        cntLabel.setStyle("-fx-text-fill: #8ca59b; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 12px;");

        card.getChildren().addAll(topRow, amtLabel, cntLabel);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle()
                .replace("rgba(18,22,32,0.85)", "rgba(28,34,48,0.95)")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
                .replace("rgba(28,34,48,0.95)", "rgba(18,22,32,0.85)")));

        // Click: show transactions for this category
        card.setOnMouseClicked(e -> showCategoryTransactions(name, total, isIncome, color));

        return card;
    }

    private void handleDeleteCategory(String categoryName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText("Delete '" + categoryName + "'?");
        alert.setContentText("Are you sure you want to delete this category? This will not delete the transactions but they will lose their category association.");
        
        // Style alert (basic)
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0d1520;");
        dialogPane.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white;"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            CAT_COLORS.remove(categoryName);
            buildCategoryGrid();
        }
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