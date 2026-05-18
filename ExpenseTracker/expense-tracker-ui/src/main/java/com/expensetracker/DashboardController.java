package com.expensetracker;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label selectedMonthLabel;
    @FXML private Slider monthSlider;
    @FXML private BarChart<Number, String> statisticsChart;
    @FXML private CategoryAxis xAxis;
    @FXML private VBox transactionsList;
    @FXML private Label balanceLabel;
    @FXML private Label incomeLabel;
    @FXML private Label expenseLabel;

    // Sidebar nav buttons
    @FXML private Button navTransactions;
    @FXML private Button navCategories;
    @FXML private Button navAnalytics;

    // "See All" hyperlink
    @FXML private Hyperlink seeAllLink;

    private final TransactionStore store = TransactionStore.getInstance();

    private final String[] months = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        monthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int mIdx = newVal.intValue() - 1;
            selectedMonthLabel.setText(months[mIdx] + " 2026");
        });

        // Fetch from backend in background thread
        new Thread(() -> {
            store.fetchTransactions();
            javafx.application.Platform.runLater(() -> {
                refreshSummaryCards();
                setupChart();
                loadRecentTransactions();
            });
        }).start();
    }

    // ── Summary Cards ──────────────────────────────────────────────────────────

    private void refreshSummaryCards() {
        if (balanceLabel != null)
            balanceLabel.setText("Rs " + formatAmount(store.balance()));
        if (incomeLabel != null)
            incomeLabel.setText("+Rs " + formatAmount(store.totalIncome()));
        if (expenseLabel != null)
            expenseLabel.setText("-Rs " + formatAmount(store.totalExpenses()));
    }

    // ── Bar Chart ─────────────────────────────────────────────────────────────

    private void setupChart() {
        statisticsChart.getData().clear();
        double[] weeks = store.weeklyExpenses();

        XYChart.Series<Number, String> expenses = new XYChart.Series<>();
        expenses.setName("Expenses");
        // For horizontal chart: X = Number, Y = String
        expenses.getData().add(new XYChart.Data<>(weeks[3], "Week 4"));
        expenses.getData().add(new XYChart.Data<>(weeks[2], "Week 3"));
        expenses.getData().add(new XYChart.Data<>(weeks[1], "Week 2"));
        expenses.getData().add(new XYChart.Data<>(weeks[0], "Week 1"));

        statisticsChart.getData().add(expenses);
    }

    // ── Recent Transactions ───────────────────────────────────────────────────

    private void loadRecentTransactions() {
        transactionsList.getChildren().clear();
        List<Transaction> all = store.getAll();
        // Show last 5
        int start = Math.max(0, all.size() - 5);
        for (int i = all.size() - 1; i >= start; i--) {
            Transaction t = all.get(i);
            String amountStr = t.isIncome()
                    ? "+Rs " + formatAmount(t.getAmount())
                    : "-Rs " + formatAmount(t.getAmount());
            transactionsList.getChildren().add(
                createTransactionRow(t.getTitle(), t.getCategory(), amountStr,
                                     t.isIncome() ? "success" : "danger")
            );
        }
    }

    private HBox createTransactionRow(String title, String category, String amount, String type) {
        HBox row = new HBox(10);
        row.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox textContainer = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #e8f5f0; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label catLabel = new Label(category);
        catLabel.setStyle("-fx-text-fill: #8ca59b; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 11px;");
        textContainer.getChildren().addAll(titleLabel, catLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(amount);
        String color = type.equals("success") ? "#00e676" : "#ff5370";
        amountLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 13px; -fx-font-weight: bold;");

        row.getChildren().addAll(textContainer, spacer, amountLabel);
        return row;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleNavTransactions() { navigateTo("transactions"); }
    @FXML private void handleNavCategories()   { navigateTo("categories"); }
    @FXML private void handleNavAnalytics()    { navigateTo("analytics"); }
    @FXML private void handleSeeAll()          { navigateTo("transactions"); }

    @FXML
    private void handleLogout() {
        navigateTo("login");
    }

    private void navigateTo(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String formatAmount(double amount) {
        return String.format("%,.0f", amount);
    }
}