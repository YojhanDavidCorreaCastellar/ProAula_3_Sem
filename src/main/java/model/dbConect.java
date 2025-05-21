/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class dbConect {
      
      
    private static final String URL = "jdbc:mysql://localhost:3306/loginair";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Método principal para obtener conexión
    public Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error al conectar a la base de datos:\n" + e.getMessage(), 
                "Error de conexión", 
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Método estático alternativo (opcional)
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Para pruebas
    public static void main(String[] args) {
        dbConect db = new dbConect();
        try (Connection conn = db.conectar()) {
            if (conn != null) {
                System.out.println("¡Conexión exitosa!");
                JOptionPane.showMessageDialog(null, "Conexión establecida");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
