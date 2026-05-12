package com.expensetracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private ComboBox<String> monthSelector;
    @FXML private BarChart<String, Number> statisticsChart;
    @FXML private CategoryAxis xAxis;
    @FXML private VBox transactionsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize Month Selector
        monthSelector.getItems().addAll("March 2026", "April 2026", "May 2026");
        monthSelector.getSelectionModel().selectLast();

        setupChart();
        loadRecentTransactions();
    }

    private void setupChart() {
        XYChart.Series<String, Number> expenses = new XYChart.Series<>();
        expenses.setName("Expenses");
        expenses.getData().add(new XYChart.Data<>("Week 1", 12000));
        expenses.getData().add(new XYChart.Data<>("Week 2", 8000));
        expenses.getData().add(new XYChart.Data<>("Week 3", 15000));
        expenses.getData().add(new XYChart.Data<>("Week 4", 5000));

        statisticsChart.getData().add(expenses);
    }

    private void loadRecentTransactions() {
        // Dummy data for structural testing
        transactionsList.getChildren().addAll(
            createTransactionRow("Groceries", "Food & Dining", "-Rs 4,500", "danger"),
            createTransactionRow("Internet Bill", "Utilities", "-Rs 3,000", "danger"),
            createTransactionRow("Freelance UI", "Income", "+Rs 25,000", "success"),
            createTransactionRow("Gym Membership", "Health", "-Rs 2,500", "danger")
        );
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
        String amountColor = type.equals("success") ? "#00e676" : "#ff5370";
        amountLabel.setStyle("-fx-text-fill: " + amountColor + "; -fx-font-family: 'Trebuchet MS'; -fx-font-size: 13px; -fx-font-weight: bold;");

        row.getChildren().addAll(textContainer, spacer, amountLabel);
        return row;
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/expensetracker/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) monthSelector.getScene().getWindow();
            Scene newScene = new Scene(root);
            
            var cssResource = getClass().getResource("/com/expensetracker/styles.css");
            if (cssResource != null) {
                newScene.getStylesheets().add(cssResource.toExternalForm());
            }
            
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

