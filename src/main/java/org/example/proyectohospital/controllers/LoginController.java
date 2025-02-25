package org.example.proyectohospital.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.proyectohospital.database.JDBC;
import org.example.proyectohospital.PacienteSession;
import org.example.proyectohospital.models.Paciente;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.example.proyectohospital.controllers.RegisterController.hashearPass;


public class LoginController {

    @FXML private TextField correoField;
    @FXML private PasswordField contrasenaField;
    @FXML private Button loginButton;
    @FXML private Label loginErrorLabel;
    @FXML private Label registerLabel;

    @FXML
    private void handleLoginButtonAction() {
        String correo = correoField.getText();
        String contrasena = contrasenaField.getText();
        String hashedPass = "";

        String query = "SELECT * FROM Pacientes WHERE Correo_Electrónico = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, correo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                hashedPass = rs.getString("Contraseña");

                Paciente paciente = new Paciente(
                    rs.getInt("ID_Paciente"),
                    rs.getString("Nombre"),
                    rs.getString("Apellidos"),
                    rs.getDate("Fecha_Nacimiento"),
                    rs.getString("DNI"),
                    rs.getString("Teléfono"),
                    rs.getString("Dirección"),
                    rs.getString("Correo_Electrónico")
                );
                PacienteSession.setCurrentUser(paciente);
            } else {
                loginErrorLabel.setVisible(true);
                loginErrorLabel.setText("Error de Inicio de Sesión. Correo electrónico o contraseña incorrectos.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!hashedPass.equals("")){
            boolean verificado = verificarPassword(contrasena, hashedPass);

            if (verificado){
                goMain();
            }else {
                loginErrorLabel.setVisible(true);
                loginErrorLabel.setText("Error de Inicio de Sesión. Correo electrónico o contraseña incorrectos.");
            }
        }
    }

    private void goMain() {
        // Cargamos la otra escena
        try {
            // Load the new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/proyectohospital/main.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Set new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goRegister() {
        // Cargamos la otra escena
        try {
            // Load the new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/proyectohospital/register.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) registerLabel.getScene().getWindow();

            // Set new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception (e.g., show error message to user)
        }
    }

    public boolean verificarPassword(String password, String hashedPassword) {
        String nuevoHash = hashearPass(password);
        return nuevoHash.equals(hashedPassword);
    }
}