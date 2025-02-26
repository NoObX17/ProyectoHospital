package org.example.proyectohospital.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.example.proyectohospital.database.JDBC;
import org.example.proyectohospital.PacienteSession;
import org.example.proyectohospital.models.Cita;
import org.example.proyectohospital.models.HistorialMedico;
import org.example.proyectohospital.models.Paciente;

import java.awt.Desktop;
import java.net.URI;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class MainController {

    public Label bienvenidaLabel;

    @FXML
    public AnchorPane paneCalendar;
    @FXML
    private VBox usuarioVBox;
    @FXML
    private TextField busquedaField;
    @FXML
    private ComboBox<String> filtroComboBox;
    @FXML
    private VBox historialContainer;

    private CalendarView calendarView;
    private Calendar citasCalendar;
    private ObservableList<HistorialMedico> historialCompleto = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Paciente paciente = PacienteSession.getCurrentUser(); // Obtenemos la sesion del paciente

        bienvenidaLabel.setText("Bienvenido/a " + paciente.getNombre() + "!");

        // Configuracion del calendario
        configurarCalendario(paciente);

        // Mostramos los datos del paciente en la tarjeta usuarioVBox
        usuarioVBox.getChildren().clear();
        usuarioVBox.getChildren().add(new Label("Nombre y Apellidos: " + paciente.getNombre() + " " + paciente.getApellidos()));
        usuarioVBox.getChildren().add(new Label("Fecha de Nacimiento: " + paciente.getFechaNacimiento().toString()));
        usuarioVBox.getChildren().add(new Label("DNI: " + paciente.getDNI()));
        usuarioVBox.getChildren().add(new Label("Teléfono: " + paciente.getTelefono()));
        usuarioVBox.getChildren().add(new Label("Dirección: " + paciente.getDireccion()));
        usuarioVBox.getChildren().add(new Label("Correo: " + paciente.getCorreo()));

        // Cargamos el historial médico completo
        cargarHistorialMedico(PacienteSession.getCurrentUser());

        // Configuramos el filtrado del historial
        filtroComboBox.getSelectionModel().selectFirst(); // Seleccionar el primer filtro por defecto

        // Escuchamos los cambios en el campo de búsqueda y el ComboBox para el filtrado dinamico
        busquedaField.textProperty().addListener((observable, oldValue, newValue) -> filtrarHistorial());
        filtroComboBox.valueProperty().addListener((observable, oldValue, newValue) -> filtrarHistorial());
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

        // Creacion de los campos de texto para editar
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

        // Mostramos la ventana y esperamos a que el usuario rellene
        dialog.showAndWait().ifPresent(response -> {
            paciente.setTelefono(telefonoField.getText());
            paciente.setDireccion(direccionField.getText());
            paciente.setCorreo(correoField.getText());

            // Guardamos cambios en la base de datos
            actualizarInfoPaciente(paciente);

            // Actualizamos la vista con los nuevos datos
            actualizarVistaPaciente(paciente);
        });
    }

    // Metodo para actualizar la base de datos con los nuevos datos del paciente
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

    // Metodo para mostrar los datos del paciente
    private void actualizarVistaPaciente(Paciente paciente) {
        usuarioVBox.getChildren().clear();
        usuarioVBox.getChildren().add(new Label("Nombre y Apellidos: " + paciente.getNombre() + " " + paciente.getApellidos()));
        usuarioVBox.getChildren().add(new Label("Fecha de Nacimiento: " + paciente.getFechaNacimiento().toString()));
        usuarioVBox.getChildren().add(new Label("DNI: " + paciente.getDNI()));
        usuarioVBox.getChildren().add(new Label("Teléfono: " + paciente.getTelefono()));
        usuarioVBox.getChildren().add(new Label("Dirección: " + paciente.getDireccion()));
        usuarioVBox.getChildren().add(new Label("Correo: " + paciente.getCorreo()));
    }

    // Metodo de configuracion del calendario
    private void configurarCalendario(Paciente paciente) {
        calendarView = new CalendarView();
        citasCalendar = new Calendar("Citas Médicas");

        // Configuración del calendario
        citasCalendar.setStyle(Calendar.Style.STYLE1);
        CalendarSource calendarSource = new CalendarSource("Hospital");
        calendarSource.getCalendars().add(citasCalendar);
        calendarView.getCalendarSources().add(calendarSource);
        calendarView.setShowAddCalendarButton(false);

        // Cargamos las citas
        cargarCitas(citasCalendar, paciente);

        // Configuramos las entradas de citas
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

        // Uso de click derecho en las entradas del calendario
        calendarView.setEntryContextMenuCallback(new Callback<>() {
            @Override
            public ContextMenu call(DateControl.EntryContextMenuParameter param) {
                Entry<?> entry = param.getEntry();
                ContextMenu contextMenu = new ContextMenu();

                MenuItem reprogramarItem = new MenuItem("Reprogramar");
                reprogramarItem.setOnAction(e -> reprogramarCita(entry, paciente));

                MenuItem cancelarItem = new MenuItem("Cancelar");
                cancelarItem.setOnAction(e -> cancelarCita(entry));

                contextMenu.getItems().addAll(reprogramarItem, cancelarItem);
                return contextMenu;
            }
        });

        // Añadimos el CalendarView al AnchorPane
        AnchorPane.setTopAnchor(calendarView, 0.0);
        AnchorPane.setBottomAnchor(calendarView, 0.0);
        AnchorPane.setLeftAnchor(calendarView, 0.0);
        AnchorPane.setRightAnchor(calendarView, 0.0);
        paneCalendar.getChildren().add(calendarView);
    }

    // Metodo para el formulario de creacion de citas
    private boolean abrirFormularioCita(Entry<String> entrada, Paciente paciente) {
        // Creamos un dialog para ingresar los datos de la cita
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Nueva Cita Médica");
        dialog.setHeaderText("Ingrese los detalles de la cita");

        // Botones del dialog
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Creamos campos para el formulario
        ComboBox<String> selectMedicos = new ComboBox<>();
        Map<Integer, String> listaMedicos = obtenerMedicos();
        selectMedicos.getItems().addAll(listaMedicos.values());

        // Si la cita existe nos dejamos utilizar el select de medicos
        if (entrada.getUserObject() != null) {
            selectMedicos.setDisable(true);
        }

        DatePicker fechaPicker = new DatePicker();
        fechaPicker.setValue(LocalDate.now());

        TextField horaField = new TextField();
        horaField.setPromptText("HH:mm");

        // Añadimos los campos al dialog junto con las Label
        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Doctor:"), selectMedicos,
                new Label("Fecha:"), fechaPicker,
                new Label("Hora (HH:mm):"), horaField
        ));

        // Obtencion de resultados del dialog
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Validamos el formato de la hora
                String horaTexto = horaField.getText();
                if (!validarFormatoHora(horaTexto)) {
                    mostrarAlerta("Error", "Formato de hora incorrecto. Use HH:mm.", Alert.AlertType.ERROR);
                    return false; // No continuar si la hora no es válida
                }

                try {
                    LocalDate fecha = fechaPicker.getValue();
                    LocalTime hora = LocalTime.parse(horaTexto);
                    LocalDateTime fechayhora = LocalDateTime.of(fecha, hora);

                    // Creamos la entrada del calendario
                    entrada.setInterval(fechayhora, fechayhora.plusMinutes(15)); // Duración de la cita de 15 minutos
                    entrada.setLocation("Hospital Ferreret");
                    String estado = "Programada";

                    // Obtenemos el médico seleccionado
                    String medicoSeleccionado = selectMedicos.getValue();
                    int idMedico = -1;
                    for (Map.Entry<Integer, String> entry : listaMedicos.entrySet()) {
                        if (entry.getValue().equals(medicoSeleccionado)) {
                            idMedico = entry.getKey();
                            break;
                        }
                    }

                    // Solo guardar la cita en la base de datos si es una nueva cita
                    if (entrada.getUserObject() == null) {
                        // Guardamos la cita en la base de datos
                        Cita cita = new Cita(paciente.getId(), idMedico, fechayhora, estado);
                        guardarCitaEnBD(cita, entrada);
                    }
                    return true;
                } catch (Exception e) {
                    mostrarAlerta("Error", "Hora no válida. Use HH:mm.", Alert.AlertType.ERROR);
                    return false;
                }
            }
            return false; // Si el usuario cancela el diálogo
        });

        // Mostramos el dialogo y esperamos el resultado
        Optional<Boolean> result = dialog.showAndWait();
        return result.orElse(false); // Devuelve false si el usuario cierra el diálogo sin confirmar
    }

    // Metodo con una expresion regular para validar el formato en HH:mm
    private boolean validarFormatoHora(String hora) {
        String regex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        return hora.matches(regex);
    }

    // Metodo para mostrar la alerta en caso de fallo
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Metodo para obtener un Map de medicos con clave id y valor nombre apellidos - especialidad
    private Map<Integer, String> obtenerMedicos() {
        Map<Integer, String> medicos = new HashMap<>();
        String query = "SELECT ID_Medico, Nombre, Apellidos, Especialidad FROM Medicos";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int idMedico = rs.getInt("ID_Medico");
                String nombreCompleto = rs.getString("Nombre") + " " + rs.getString("Apellidos");
                String especialidad = rs.getString("Especialidad");
                String medicoConEspecialidad = nombreCompleto + " - " + especialidad;
                medicos.put(idMedico, medicoConEspecialidad);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medicos;
    }

    // Metodo para guardar las citas en la base de datos
    private void guardarCitaEnBD(Cita cita, Entry<String> entry) {
        String sql = "INSERT INTO Citas (ID_Paciente, ID_Medico, Fecha_Cita, Hora_Cita, Estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, cita.getIdPaciente());
            pstmt.setInt(2, cita.getIdMedico());
            pstmt.setDate(3, java.sql.Date.valueOf(cita.getFechaHora().toLocalDate()));
            pstmt.setTime(4, java.sql.Time.valueOf(cita.getFechaHora().toLocalTime()));
            pstmt.setString(5, cita.getEstado());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        String idCita = String.valueOf(generatedKeys.getInt(1));
                        entry.setUserObject(idCita);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo con la logica para reprogramar una cita
    private void reprogramarCita(Entry<?> entrada, Paciente paciente) {
        if (entrada != null) {
            Entry<String> citaEntrada = (Entry<String>) entrada;
            boolean citaConfirmada = abrirFormularioCita(citaEntrada, paciente);
            if (citaConfirmada) {
                // Obtenemos el ID_Cita almacenada en el userObject del entry
                String idCita = (String) citaEntrada.getUserObject();
                // Actualizamos la cita en la base de datos
                String sql = "UPDATE Citas SET Fecha_Cita = ?, Hora_Cita = ?, Estado = ? WHERE ID_Cita = ?";
                try (Connection conn = JDBC.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setDate(1, java.sql.Date.valueOf(citaEntrada.getStartDate()));
                    pstmt.setTime(2, java.sql.Time.valueOf(citaEntrada.getStartTime()));
                    pstmt.setString(3, "Reprogramada");
                    pstmt.setString(4, idCita);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Metodo con toda la logica para cancelar una cita
    private void cancelarCita(Entry<?> entrada) {
        // Obtenemos el ID_Cita almacenada en el userObject del entry
        String idCita = (String) entrada.getUserObject();
        // Cambiamos el estado de la cita a "Cancelada"
        String sql = "UPDATE Citas SET Estado = 'Cancelada' WHERE ID_Cita = ?";
        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idCita);
            pstmt.executeUpdate();
            entrada.setCalendar(null); // Eliminamos la cita del calendario
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo para cargar citas en el calendario
    private void cargarCitas(Calendar citasCalendar, Paciente paciente) {
        // Seleccionamos las citas que no tengan el estado de "Cancelada"
        String query = "SELECT * FROM Citas WHERE ID_Paciente = ? AND Estado != 'Cancelada'";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, paciente.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String idCita = rs.getString("ID_Cita");
                LocalDate fecha = rs.getDate("Fecha_Cita").toLocalDate();
                LocalTime hora = rs.getTime("Hora_Cita").toLocalTime();

                // Creamos una entrada para el calendario
                Entry<String> citaEntry = new Entry<>("Cita #" + idCita);
                citaEntry.setInterval(fecha);
                citaEntry.setInterval(hora, hora.plusMinutes(15)); // Duracion de la cita de 15 minutos
                citaEntry.setLocation("Hospital Ferreret");
                citaEntry.setUserObject(idCita);

                // Añadimos la entrada al calenadrio
                citaEntry.setCalendar(citasCalendar);

                // Ponemos titulo de la entrada
                citaEntry.setTitle("Cita médico");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo para cargar el historial medico del paciente
    private void cargarHistorialMedico(Paciente paciente) {
        String query = "SELECT Fecha_Visita, Diagnóstico, Tratamiento FROM Historial_Medico WHERE ID_Paciente = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, paciente.getId());
            ResultSet rs = pstmt.executeQuery();

            historialCompleto.clear();

            while (rs.next()) {
                String fechaVisita = rs.getDate("Fecha_Visita").toString();
                String diagnostico = rs.getString("Diagnóstico");
                String tratamiento = rs.getString("Tratamiento");

                // Añadimos al historial completo
                historialCompleto.add(new HistorialMedico(fechaVisita, diagnostico, tratamiento));
            }

            // Mostramos el historial completo inicialmente
            mostrarHistorial(historialCompleto);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo para mostrar el historial medico del paciente
    private void mostrarHistorial(ObservableList<HistorialMedico> historial) {
        historialContainer.getChildren().clear();

        for (HistorialMedico entrada : historial) {
            HBox tarjeta = crearTarjetaHistorial(entrada.getFechaVisita(), entrada.getDiagnostico(), entrada.getTratamiento());
            historialContainer.getChildren().add(tarjeta);
        }
    }

    // Metodo para filtrar la vista del historial medico
    private void filtrarHistorial() {
        String terminoBusqueda = busquedaField.getText().toLowerCase();
        String filtroSeleccionado = filtroComboBox.getValue();

        ObservableList<HistorialMedico> historialFiltrado = FXCollections.observableArrayList();

        for (HistorialMedico entrada : historialCompleto) {
            boolean coincide = false;
            // Select de los diferentes filtros
            switch (filtroSeleccionado) {
                case "Fecha":
                    coincide = entrada.getFechaVisita().toLowerCase().contains(terminoBusqueda);
                    break;
                case "Diagnóstico":
                    coincide = entrada.getDiagnostico().toLowerCase().contains(terminoBusqueda);
                    break;
                case "Tratamiento":
                    coincide = entrada.getTratamiento().toLowerCase().contains(terminoBusqueda);
                    break;
            }

            if (coincide) {
                historialFiltrado.add(entrada);
            }
        }

        // Mostramos el historial filtrado
        mostrarHistorial(historialFiltrado);
    }

    // Metodo para crear una tarjeta de historial médico
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

    // Metodos para los botones del footer con los iconos de redes sociales
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
