package com.devopsservicedesk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ServiceDeskApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServiceDeskApp.class.getResource("/layout_tiket.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        stage.setTitle("DevOps Service Desk - Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}