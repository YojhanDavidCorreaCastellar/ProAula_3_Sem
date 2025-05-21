package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class userConect {
    
    public static boolean registrarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, email, contraseña, fecha_registro) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setString(3, usuario.getContraseña());
            pstmt.setDate(4, new java.sql.Date(usuario.getFecha_registro().getTime()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setId_usuario(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    
    public static boolean registrarCliente(Cliente cliente) {
        
        if (!registrarUsuario(cliente)) {
            return false;
        }
        
        // Luego registramos los datos adicionales del cliente
        String sql = "INSERT INTO clientes (id_usuario, celular, username) VALUES (?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cliente.getId_usuario());
            pstmt.setString(2, cliente.getCelular());
            pstmt.setString(3, cliente.getUserName());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}