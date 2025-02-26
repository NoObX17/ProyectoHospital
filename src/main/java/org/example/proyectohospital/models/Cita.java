package org.example.proyectohospital.models;

import java.time.LocalDateTime;

// Modelo de clase Cita basado en la tabla de base de datos
public class Cita {
    private int id;
    private int idPaciente;
    private int idMedico;
    private LocalDateTime fechaHora;
    private String estado;

    // Constructor
    public Cita(int idPaciente, int idMedico, LocalDateTime fechaHora, String estado) {
        this.idPaciente = idPaciente;
        this.idMedico = idMedico;
        this.fechaHora = fechaHora;
        this.estado = estado;
    }

    // Getters
    public int getIdPaciente() {
        return idPaciente;
    }

    public int getIdMedico() {
        return idMedico;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getEstado() {
        return estado;
    }
}