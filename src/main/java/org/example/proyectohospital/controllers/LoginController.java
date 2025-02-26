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

    @FXML
    private TextField correoField;
    @FXML
    private PasswordField contrasenaField;
    @FXML
    private Button loginButton;
    @FXML
    private Label loginErrorLabel;
    @FXML
    private Label registerLabel;

    // Metodo con el comportamiento del boton de Login
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
                // Guardamos una sesion del paciente para obtener sus datos en cualquier momento
                PacienteSession.setCurrentUser(paciente);
            } else {
                loginErrorLabel.setVisible(true);
                loginErrorLabel.setText("Error de Inicio de Sesión. Correo electrónico o contraseña incorrectos.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!hashedPass.equals("")) {
            boolean verificado = verificarPassword(contrasena, hashedPass);

            if (verificado) {
                goMain();
            } else {
                loginErrorLabel.setVisible(true);
                loginErrorLabel.setText("Error de Inicio de Sesión. Correo electrónico o contraseña incorrectos.");
            }
        }
    }

    private void goMain() {
        // Cargamos la escena del Main Menu y la abrimos
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/proyectohospital/main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();

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
        // Cargamos la escena del Register y la abrimos
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/proyectohospital/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) registerLabel.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo simple para verificar la contraseña de la base de datos y la introducida por el usuario
    public boolean verificarPassword(String password, String hashedPassword) {
        String nuevoHash = hashearPass(password);
        return nuevoHash.equals(hashedPassword);
    }
}