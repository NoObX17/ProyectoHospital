package org.example.proyectohospital;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.example.proyectohospital.models.Paciente;

import java.awt.*;
import java.net.URI;

public class MainController {

    public Label bienvenidaLabel;
    @FXML
    private ImageView facebookIcon;
    @FXML
    private ImageView twitterIcon;
    @FXML
    private ImageView instagramIcon;

    @FXML
    public void initialize(){
        Paciente paciente = PacienteSession.getCurrentUser();

        bienvenidaLabel.setText("Bienvenido/a " + paciente.getNombre() + "!");
    }

    @FXML
    private void facebookClicked() {
        System.out.println("facebook clicked");
        try {
            Desktop.getDesktop().browse(new URI("https://www.facebook.com"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void twitterClicked() {
        System.out.println("twitter clicked");
        try {
            Desktop.getDesktop().browse(new URI("https://www.x.com"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void instagramClicked() {
        System.out.println("instagram clicked");
        try {
            Desktop.getDesktop().browse(new URI("https://www.instagram.com"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
