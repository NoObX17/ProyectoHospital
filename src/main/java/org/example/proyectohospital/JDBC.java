package org.example.proyectohospital;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBC {
    private static final String URL = "jdbc:mysql://192.168.56.112:3306/hospital";
    private static final String USER = "hugo";
    private static final String PASSWORD = "HugoNazi18.";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
