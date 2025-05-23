package model;

import model.dbConect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class userSearch {

    public boolean accesoUsuario(String user, String pass, String rol) {
        dbConect db = new dbConect();
        boolean accesoCorrecto = false;
        
        // Verificación en tabla 'usuarioss' (ajusta el nombre si es diferente)
        accesoCorrecto = verificarCredenciales(db, "usuarioss", "usuario", user, pass, rol);
        
        // Si es administrador y no se encontró, verificar en tabla 'administradores'
        if (!accesoCorrecto && rol.equalsIgnoreCase("administrador")) {
            accesoCorrecto = verificarCredenciales(db, "administradores", "usuario", user, pass, rol);
        }
        
        // Si aún no se encuentra, mostrar mensaje
        if (!accesoCorrecto) {
            JOptionPane.showMessageDialog(null, 
                "Usuario no encontrado o credenciales incorrectas", 
                "Error de autenticación", 
                JOptionPane.WARNING_MESSAGE);
        }
        
        return accesoCorrecto;
    }

    private boolean verificarCredenciales(dbConect db, String tabla, String columnaUsuario, 
                                        String user, String pass, String rol) {
        String sql = "SELECT 1 FROM " + tabla + " WHERE " + columnaUsuario + " = ? AND contraseña = ? AND rol = ?";
        
        try (Connection cn = db.conectar();
             PreparedStatement pst = cn.prepareStatement(sql)) {
            
            pst.setString(1, user);
            pst.setString(2, pass);
            pst.setString(3, rol);
            
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next(); // Retorna true si encuentra coincidencia
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error al verificar credenciales en tabla " + tabla + ":\n" + e.getMessage(), 
                "Error de base de datos", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}