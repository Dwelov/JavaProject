package com.expensetracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"), 1100, 700);



        
        // Load CSS if available
        try {
            var cssResource = App.class.getResource("/com/expensetracker/styles.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }
        } catch (Exception e) {
            // CSS file not found, continue without it
            System.err.println("Warning: CSS file not found: " + e.getMessage());
        }
        

        stage.setTitle("FinanceOS");
        stage.setResizable(true);

        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
            App.class.getResource("/com/expensetracker/" + fxml + ".fxml")
        );
  
        
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
    
}