package com.expensetracker;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.ResourceBundle;

public class AnalyticsController implements Initializable {

    @FXML private Label   totalIncomeLabel;
    @FXML private Label   totalExpenseLabel;
    @FXML private Label   netBalanceLabel;
    @FXML private Label   savingsRateLabel;

    @FXML private BarChart<Number, String> weeklyChart;
    @FXML private CategoryAxis weekAxis;
    @FXML private PieChart     categoryPieChart;

    @FXML private VBox    categoryBreakdownList;

    private final TransactionStore store = TransactionStore.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateSummaryCards();
        populateWeeklyChart();
        populatePieChart();
        populateCategoryBreakdown();
    }

    // ── Summary Cards ─────────────────────────────────────────────────────────

    private void populateSummaryCards() {
        double income   = store.totalIncome();
        double expenses = store.totalExpenses();
        double balance  = store.balance();
        double rate     = income > 0 ? (balance / income) * 100 : 0;

        totalIncomeLabel.setText("+Rs " + fmt(income));
        totalExpenseLabel.setText("-Rs " + fmt(expenses));
        netBalanceLabel.setText("Rs " + fmt(balance));
        netBalanceLabel.setStyle(balance >= 0
                ? "-fx-text-fill: #00e676; -fx-font-family: Georgia; -fx-font-size: 28px; -fx-font-weight: bold;"
                : "-fx-text-fill: #ff5370; -fx-font-family: Georgia; -fx-font-size: 28px; -fx-font-weight: bold;");
        savingsRateLabel.setText(String.format("%.1f%%", rate));
    }

    // ── Weekly Bar Chart ──────────────────────────────────────────────────────

    private void populateWeeklyChart() {
        weeklyChart.getData().clear();
        double[] weeks = store.weeklyExpenses();

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Expenses");
        series.getData().add(new XYChart.Data<>(weeks[3], "Week 4"));
        series.getData().add(new XYChart.Data<>(weeks[2], "Week 3"));
        series.getData().add(new XYChart.Data<>(weeks[1], "Week 2"));
        series.getData().add(new XYChart.Data<>(weeks[0], "Week 1"));
        weeklyChart.getData().add(series);
    }

    // ── Pie Chart ─────────────────────────────────────────────────────────────

    private void populatePieChart() {
        categoryPieChart.getData().clear();
        Map<String, Double> byCategory = store.expensesByCategory();

        byCategory.forEach((cat, total) ->
            categoryPieChart.getData().add(new PieChart.Data(cat + "\nRs " + fmt(total), total))
        );

        // Style: transparent background, white labels
        categoryPieChart.setStyle("-fx-background-color: transparent;");
        categoryPieChart.setLabelLineLength(12);
        categoryPieChart.setLabelsVisible(true);
    }

    // ── Category Breakdown Table ──────────────────────────────────────────────

    private void populateCategoryBreakdown() {
        categoryBreakdownList.getChildren().clear();

        Map<String, Double>   expMap   = store.expensesByCategory();
        double totalExp = store.totalExpenses();

        // Count transactions per category
        Map<String, Long> countMap = new LinkedHashMap<>();
        store.getAll().stream()
             .filter(t -> !t.isIncome())
             .forEach(t -> countMap.merge(t.getCategory(), 1L, Long::sum));

        // Sort by amount descending
        expMap.entrySet().stream()
              .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
              .forEach(entry -> {
                  String cat   = entry.getKey();
                  double amt   = entry.getValue();
                  long   count = countMap.getOrDefault(cat, 0L);
                  double pct   = totalExp > 0 ? (amt / totalExp) * 100 : 0;

                  categoryBreakdownList.getChildren().add(buildBreakdownRow(cat, count, amt, pct));
              });
    }

    private HBox buildBreakdownRow(String category, long count, double amount, double pct) {
        HBox row = new HBox(0);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10 4 10 4; -fx-background-color: rgba(255,255,255,0.02);" +
                     "-fx-background-radius: 8;");

        // Icon + name
        HBox nameCell = new HBox(10);
        nameCell.setPrefWidth(220);
        nameCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label icon = new Label(categoryIcon(category));
        icon.setStyle("-fx-font-size: 16px;");
        Label name = new Label(category);
        name.setStyle("-fx-text-fill: #e8f5f0; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 13px;");
        nameCell.getChildren().addAll(icon, name);

        // Transaction count
        Label countLabel = new Label(count + " txns");
        countLabel.setPrefWidth(120);
        countLabel.setStyle("-fx-text-fill: #8ca59b; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 13px;");

        // Amount
        Label amtLabel = new Label("-Rs " + fmt(amount));
        amtLabel.setPrefWidth(150);
        amtLabel.setStyle("-fx-text-fill: #ff5370; -fx-font-family: 'Trebuchet MS';" +
                          " -fx-font-size: 13px; -fx-font-weight: bold;");

        // Progress bar + percentage
        VBox pctBox = new VBox(4);
        HBox.setHgrow(pctBox, Priority.ALWAYS);
        Label pctLabel = new Label(String.format("%.1f%%", pct));
        pctLabel.setStyle("-fx-text-fill: #8ca59b; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 11px;");

        // Simple progress bar using a styled region
        StackPane barBg = new StackPane();
        barBg.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 4;");
        barBg.setPrefHeight(6);
        barBg.setMaxWidth(Double.MAX_VALUE);
        Region fill = new Region();
        fill.setStyle("-fx-background-color: #00e676; -fx-background-radius: 4;");
        fill.setPrefHeight(6);
        // Width will be set proportionally; we use a HBox trick
        HBox barFill = new HBox(fill);
        barFill.setMaxWidth(Double.MAX_VALUE);
        // We can't bind easily without FX properties, so set min width
        fill.setPrefWidth(Math.max(4, pct * 2)); // scale: 100% = 200px max
        barBg.getChildren().add(barFill);

        pctBox.getChildren().addAll(pctLabel, barBg);

        row.getChildren().addAll(nameCell, countLabel, amtLabel, pctBox);
        return row;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleNavDashboard()    { navigateTo("dashboard"); }
    @FXML private void handleNavTransactions() { navigateTo("transactions"); }
    @FXML private void handleNavCategories()   { navigateTo("categories"); }
    @FXML private void handleLogout()          { navigateTo("login"); }

    private void navigateTo(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String fmt(double v) { return String.format("%,.0f", v); }

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