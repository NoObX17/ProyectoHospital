package org.example.proyectohospital;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Inicio extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("login.fxml")));

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/proyectohospital/images/logohospital.png")));
            primaryStage.getIcons().add(icon);
            primaryStage.setTitle("Ferreret Pacientes");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
