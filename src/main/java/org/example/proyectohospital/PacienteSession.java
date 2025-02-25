package org.example.proyectohospital;

import org.example.proyectohospital.models.Paciente;

public class PacienteSession {
    private static Paciente currentUser;

    public static void setCurrentUser(Paciente user) {
        currentUser = user;
    }

    public static Paciente getCurrentUser() {
        return currentUser;
    }
}
