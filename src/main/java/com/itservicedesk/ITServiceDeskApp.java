package com.itservicedesk;

import com.itservicedesk.config.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ITServiceDeskApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseConfig.createTables();
        FXMLLoader fxmlLoader = new FXMLLoader(ITServiceDeskApp.class.getResource("/layout/ticket_dashboard.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1366, 768);
        scene.getStylesheets().add(ITServiceDeskApp.class.getResource("/css/styles.css").toExternalForm());
        stage.setTitle("IT Service Desk");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}