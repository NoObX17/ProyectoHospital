package org.example.proyectohospital.models;

import java.util.Date;

// Modelo de clase Paciente basado en la base de datos
public class Paciente {
    private int id;
    private String nombre;
    private String apellidos;
    private Date fechaNacimiento;
    private String DNI;
    private String telefono;
    private String direccion;
    private String correo;

    // Constructor
    public Paciente(int id, String nombre, String apellidos, Date fechaNacimiento, String DNI, String telefono,
                    String direccion, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.DNI = DNI;
        this.telefono = telefono;
        this.direccion = direccion;
        this.correo = correo;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getDNI() {
        return DNI;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}