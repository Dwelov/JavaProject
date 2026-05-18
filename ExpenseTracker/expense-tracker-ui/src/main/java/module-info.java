module com.expensetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.net.http;

    opens com.expensetracker to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.expensetracker;
}
