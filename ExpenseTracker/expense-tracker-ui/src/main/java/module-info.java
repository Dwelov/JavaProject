module com.expensetracker {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.expensetracker to javafx.fxml;
    exports com.expensetracker;
}
