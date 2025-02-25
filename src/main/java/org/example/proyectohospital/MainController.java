package org.example.proyectohospital;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.proyectohospital.models.Cita;
import org.example.proyectohospital.models.Paciente;


import java.awt.Desktop;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class MainController {

    public Label bienvenidaLabel;

    @FXML public AnchorPane paneCalendar;
    @FXML private VBox usuarioVBox;
    @FXML private VBox historialContainer;

    private CalendarView calendarView;
    private Calendar citasCalendar;

    @FXML
    public void initialize() {
        Paciente paciente = PacienteSession.getCurrentUser();
        CalendarView calendarView = new CalendarView();

        bienvenidaLabel.setText("Bienvenido/a " + paciente.getNombre() + "!");

        // paneCalendar
        configurarCalendario(paciente);

        // Mostrar los datos del paciente en usuarioVBox
        usuarioVBox.getChildren().clear();
        usuarioVBox.getChildren().add(new Label("Nombre y Apellidos: " + paciente.getNombre() + " " + paciente.getApellidos()));
        usuarioVBox.getChildren().add(new Label("Fecha de Nacimiento: " + paciente.getFechaNacimiento().toString()));
        usuarioVBox.getChildren().add(new Label("DNI: " + paciente.getDNI()));
        usuarioVBox.getChildren().add(new Label("Teléfono: " + paciente.getTelefono()));
        usuarioVBox.getChildren().add(new Label("Dirección: " + paciente.getDireccion()));
        usuarioVBox.getChildren().add(new Label("Correo: " + paciente.getCorreo()));

        // Cargar el historial médico del paciente
        cargarHistorialMedico(paciente);
    }

    private void configurarCalendario(Paciente paciente) {
        calendarView = new CalendarView();
        citasCalendar = new Calendar("Citas Médicas");

        // Configuración del calendario
        citasCalendar.setStyle(Calendar.Style.STYLE1);
        CalendarSource calendarSource = new CalendarSource("Hospital");
        calendarSource.getCalendars().add(citasCalendar);
        calendarView.getCalendarSources().add(calendarSource);

        //Cargamos las citas
        cargarCitas(citasCalendar, paciente);

        // Configurar entradas de citas
        calendarView.setEntryFactory(param -> {
            Entry<String> newEntry = new Entry<>("Cita médico");
            newEntry.setInterval(LocalDate.now());
            boolean citaConfirmada = abrirFormularioCita(newEntry, paciente);

            if (citaConfirmada) {
                return newEntry;
            } else {
                return null;
            }
        });

        // Añadir el CalendarView al AnchorPane
        AnchorPane.setTopAnchor(calendarView, 0.0);
        AnchorPane.setBottomAnchor(calendarView, 0.0);
        AnchorPane.setLeftAnchor(calendarView, 0.0);
        AnchorPane.setRightAnchor(calendarView, 0.0);
        paneCalendar.getChildren().add(calendarView);
    }

    private boolean abrirFormularioCita(Entry<String> entrada, Paciente paciente) {
        // Crear un diálogo simple para ingresar los datos de la cita
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Nueva Cita Médica");
        dialog.setHeaderText("Ingrese los detalles de la cita");

        // Botones del diálogo
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Crear campos para el formulario
        ComboBox<String> selectMedicos = new ComboBox<>();
        Map<Integer, String> listaMedicos = obtenerMedicos();
        selectMedicos.getItems().addAll(listaMedicos.values());

        DatePicker fechaPicker = new DatePicker();
        fechaPicker.setValue(LocalDate.now());

        TextField horaField = new TextField();
        horaField.setPromptText("HH:mm");

        // Añadir campos al diálogo
        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Doctor:"), selectMedicos,
                new Label("Fecha:"), fechaPicker,
                new Label("Hora (HH:mm):"), horaField
        ));

        // Manejar el resultado del diálogo
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Validar el formato de la hora
                String horaTexto = horaField.getText();
                if (!validarFormatoHora(horaTexto)) {
                    mostrarAlerta("Error", "Formato de hora incorrecto. Use HH:mm.", Alert.AlertType.ERROR);
                    return false; // No continuar si la hora no es válida
                }

                try {
                    LocalDate fecha = fechaPicker.getValue();
                    LocalTime hora = LocalTime.parse(horaTexto); // Convertir la hora a LocalTime
                    LocalDateTime fechayhora = LocalDateTime.of(fecha, hora);

                    // Actualizar la entrada del calendario
                    entrada.setInterval(fechayhora);
                    entrada.setLocation("Hospital Ferreret");
                    String estado = "Programada";

                    // Obtener el médico seleccionado
                    String medicoSeleccionado = selectMedicos.getValue();
                    int idMedico = -1;
                    for (Map.Entry<Integer, String> entry : listaMedicos.entrySet()) {
                        if (entry.getValue().equals(medicoSeleccionado)) {
                            idMedico = entry.getKey();
                            break;
                        }
                    }

                    // Guardar la cita en la base de datos
                    Cita cita = new Cita(paciente.getId(), idMedico, fechayhora, estado);
                    guardarCitaEnBD(cita);

                    return true; // Cita confirmada
                } catch (Exception e) {
                    mostrarAlerta("Error", "Hora no válida. Use HH:mm.", Alert.AlertType.ERROR);
                    return false; // No continuar si hay un error al parsear la hora
                }
            }
            return false; // Si el usuario cancela el diálogo
        });

        // Mostrar el diálogo y devolver el resultado
        Optional<Boolean> result = dialog.showAndWait();
        return result.orElse(false); // Devuelve false si el usuario cierra el diálogo sin confirmar
    }

    private boolean validarFormatoHora(String hora) {
        // Expresión regular para validar el formato HH:mm
        String regex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        return hora.matches(regex);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private Map<Integer, String> obtenerMedicos() {
        Map<Integer, String> medicos = new HashMap<>(); // Usar un Map para clave-valor
        String query = "SELECT ID_Medico, Nombre, Apellidos, Especialidad FROM Medicos";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int idMedico = rs.getInt("ID_Medico");
                String nombreCompleto = rs.getString("Nombre") + " " + rs.getString("Apellidos");
                String especialidad = rs.getString("Especialidad");
                String medicoConEspecialidad = nombreCompleto + " - " + especialidad; // Formato: "Nombre Apellidos - Especialidad"
                medicos.put(idMedico, medicoConEspecialidad); // Agregar al Map
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return medicos;
    }

    private void guardarCitaEnBD(Cita cita) {
        String sql = "INSERT INTO Citas (ID_Paciente, ID_Medico, Fecha_Cita, Hora_Cita, Estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cita.getIdPaciente());
            pstmt.setInt(2, cita.getIdMedico());
            pstmt.setDate(3, java.sql.Date.valueOf(cita.getFechaHora().toLocalDate()));
            pstmt.setTime(4, java.sql.Time.valueOf(cita.getFechaHora().toLocalTime()));
            pstmt.setString(5, cita.getEstado());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarCitas(Calendar citasCalendar, Paciente paciente) {
        String query = "SELECT * FROM Citas WHERE ID_Paciente = ?"; // Consulta corregida

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, paciente.getId()); // Asignar el ID del paciente al PreparedStatement
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int idCita = rs.getInt("ID_Cita");
                LocalDate fecha = rs.getDate("Fecha_Cita").toLocalDate();
                LocalTime hora = rs.getTime("Hora_Cita").toLocalTime();
                LocalDateTime fechaHora = LocalDateTime.of(fecha, hora); // Combinar fecha y hora

                // Crear una entrada para el calendario
                Entry<String> citaEntry = new Entry<>("Cita #" + idCita);
                citaEntry.setInterval(fechaHora); // Establecer la fecha y hora de la cita
                citaEntry.setLocation("Hospital Ferreret");
                citaEntry.setCalendar(citasCalendar); // Asignar la entrada al calendario de citas

                // Opcional: Añadir detalles adicionales a la entrada
                citaEntry.setTitle("Cita médico");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditarButtonAction() {
        abrirVentanaEdicion();
    }

    private void abrirVentanaEdicion() {
        Paciente paciente = PacienteSession.getCurrentUser();

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Editar Datos");
        dialog.setHeaderText("Editar teléfono, dirección o correo");

        // Crear campos de texto para editar
        TextField telefonoField = new TextField(paciente.getTelefono());
        TextField direccionField = new TextField(paciente.getDireccion());
        TextField correoField = new TextField(paciente.getCorreo());

        VBox vbox = new VBox(10,
                new Label("Teléfono:"), telefonoField,
                new Label("Dirección:"), direccionField,
                new Label("Correo:"), correoField
        );

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/example/proyectohospital/styles/styles.css").toExternalForm());

        // Mostrar la ventana y esperamos a que el usuario rellene
        dialog.showAndWait().ifPresent(response -> {
            paciente.setTelefono(telefonoField.getText());
            paciente.setDireccion(direccionField.getText());
            paciente.setCorreo(correoField.getText());

            // Guardar cambios en la base de datos
            actualizarInfoPaciente(paciente);

            // Actualizar la vista con los nuevos datos
            actualizarVistaPaciente(paciente);
        });
    }

    private void actualizarInfoPaciente(Paciente paciente) {
        String query = "UPDATE Pacientes SET Teléfono = ?, Dirección = ?, Correo_Electrónico = ? WHERE ID_Paciente = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, paciente.getTelefono());
            pstmt.setString(2, paciente.getDireccion());
            pstmt.setString(3, paciente.getCorreo());
            pstmt.setInt(4, paciente.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void actualizarVistaPaciente(Paciente paciente) {
        // Actualizar la vista con los nuevos datos del paciente
        usuarioVBox.getChildren().clear();
        usuarioVBox.getChildren().add(new Label("Nombre y Apellidos: " + paciente.getNombre() + " " + paciente.getApellidos()));
        usuarioVBox.getChildren().add(new Label("Fecha de Nacimiento: " + paciente.getFechaNacimiento().toString()));
        usuarioVBox.getChildren().add(new Label("DNI: " + paciente.getDNI()));
        usuarioVBox.getChildren().add(new Label("Teléfono: " + paciente.getTelefono()));
        usuarioVBox.getChildren().add(new Label("Dirección: " + paciente.getDireccion()));
        usuarioVBox.getChildren().add(new Label("Correo: " + paciente.getCorreo()));
    }

    private void cargarHistorialMedico(Paciente paciente) {
        String query = "SELECT Fecha_Visita, Diagnóstico, Tratamiento FROM Historial_Medico WHERE ID_Paciente = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, paciente.getId());
            ResultSet rs = pstmt.executeQuery();

            // Limpiar el contenedor antes de añadir nuevas tarjetas
            historialContainer.getChildren().clear();

            while (rs.next()) {
                String fechaVisita = rs.getDate("Fecha_Visita").toString();
                String diagnostico = rs.getString("Diagnóstico");
                String tratamiento = rs.getString("Tratamiento");

                // Crear una tarjeta para cada entrada del historial
                HBox tarjeta = crearTarjetaHistorial(fechaVisita, diagnostico, tratamiento);
                historialContainer.getChildren().add(tarjeta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para crear una tarjeta de historial médico
    private HBox crearTarjetaHistorial(String fechaVisita, String diagnostico, String tratamiento) {
        HBox tarjeta = new HBox(15);
        tarjeta.getStyleClass().add("tarjeta-historial");

        // Contenido de la tarjeta
        VBox contenido = new VBox(10);
        contenido.getChildren().addAll(
                new Label("Fecha de Visita: " + fechaVisita) {{
                    getStyleClass().add("titulo-historial");
                }},
                new Label("Diagnóstico:") {{
                    getStyleClass().add("subtitulo-historial");
                }},
                new Label(diagnostico) {{
                    getStyleClass().add("contenido-historial");
                }},
                new Label("Tratamiento:") {{
                    getStyleClass().add("subtitulo-historial");
                }},
                new Label(tratamiento) {{
                    getStyleClass().add("contenido-historial");
                }}
        );

        tarjeta.getChildren().add(contenido);
        return tarjeta;
    }

    @FXML
    private void facebookClicked() {
        try {
            Desktop.getDesktop().browse(new URI("https://www.facebook.com"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void twitterClicked() {
        try {
            Desktop.getDesktop().browse(new URI("https://www.x.com"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void instagramClicked() {
        try {
            Desktop.getDesktop().browse(new URI("https://www.instagram.com"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
