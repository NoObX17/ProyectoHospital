package org.example.proyectohospital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class RegisterController {

    @FXML private TextField nombreField;
    @FXML private TextField apellidosField;
    @FXML private DatePicker fechaNacimientoPicker;
    @FXML private TextField dniField;
    @FXML private TextField telefonoField;
    @FXML private TextField direccionField;
    @FXML private TextField correoField;
    @FXML private PasswordField contrasenaField;
    @FXML private Label loginLabel;

    @FXML
    private void handleRegisterButtonAction() {
        String nombre = nombreField.getText();
        String apellidos = apellidosField.getText();
        String fechaNacimiento = fechaNacimientoPicker.getValue() != null
                ? fechaNacimientoPicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "";
        String dni = dniField.getText();
        String telefono = telefonoField.getText();
        String direccion = direccionField.getText();
        String correo = correoField.getText();
        String contrasena = contrasenaField.getText();

        String hashContra = hashearPass(contrasena);
        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO Pacientes (Nombre, Apellidos, Fecha_Nacimiento, DNI, Teléfono, Dirección, Correo_Electrónico, Contraseña) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, apellidos);
            pstmt.setString(3, fechaNacimiento);
            pstmt.setString(4, dni);
            pstmt.setString(5, telefono);
            pstmt.setString(6, direccion);
            pstmt.setString(7, correo);
            pstmt.setString(8, hashContra);
            pstmt.executeUpdate();
            System.out.println("Usuario registrado con éxito.");
            goLogin();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goLogin() {
        // Cargamos la otra escena
        try {
            // Load the new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) loginLabel.getScene().getWindow();

            // Set new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception (e.g., show error message to user)
        }
    }

    public static String hashearPass(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar la contraseña", e);
        }
    }

}