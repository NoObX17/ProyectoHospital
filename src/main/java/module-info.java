module org.example.proyectohospital {
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires com.calendarfx.view;
    requires java.desktop;

    opens org.example.proyectohospital to javafx.fxml;
    opens org.example.proyectohospital.models to javafx.base;
    exports org.example.proyectohospital;
    exports org.example.proyectohospital.controllers;
    opens org.example.proyectohospital.controllers to javafx.fxml;
    exports org.example.proyectohospital.database;
    opens org.example.proyectohospital.database to javafx.fxml;
}