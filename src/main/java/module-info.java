module org.example.proyectohospital {
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires com.calendarfx.view;
    requires java.desktop;

    opens org.example.proyectohospital to javafx.fxml;
    exports org.example.proyectohospital;
}