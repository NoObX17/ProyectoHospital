package org.example.proyectohospital.models;

public class HistorialMedico {
    private String fechaVisita;
    private String diagnostico;
    private String tratamiento;

    public HistorialMedico(String fechaVisita, String diagnostico, String tratamiento) {
        this.fechaVisita = fechaVisita;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
    }

    // Getters
    public String getFechaVisita() {
        return fechaVisita;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }
}
