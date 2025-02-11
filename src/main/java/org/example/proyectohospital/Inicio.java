package org.example.proyectohospital;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Inicio extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("register.fxml")));

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.setTitle("Register");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
