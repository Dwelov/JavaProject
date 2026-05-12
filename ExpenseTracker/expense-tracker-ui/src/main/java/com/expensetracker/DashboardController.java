package com.expensetracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private ComboBox<String> monthSelector;
    @FXML private BarChart<String, Number> statisticsChart;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        monthSelector.getItems().addAll("March 2026", "April 2026", "May 2026");
        monthSelector.getSelectionModel().selectLast();

        refreshSummaryCards();
        setupChart();
        loadRecentTransactions();
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

        XYChart.Series<String, Number> expenses = new XYChart.Series<>();
        expenses.setName("Expenses");
        expenses.getData().add(new XYChart.Data<>("Week 1", weeks[0]));
        expenses.getData().add(new XYChart.Data<>("Week 2", weeks[1]));
        expenses.getData().add(new XYChart.Data<>("Week 3", weeks[2]));
        expenses.getData().add(new XYChart.Data<>("Week 4", weeks[3]));

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
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/expensetracker/" + fxml + ".fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) monthSelector.getScene().getWindow();
            Scene newScene = new Scene(root);
            applyCss(newScene);
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyCss(Scene scene) {
        try {
            var css = getClass().getResource("/com/expensetracker/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) { }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String formatAmount(double amount) {
        return String.format("%,.0f", amount);
    }
}