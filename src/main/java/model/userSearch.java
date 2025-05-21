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
        Connection cn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            cn = db.conectar();
            String sql = "SELECT usuario, email, contraseña, celular, rol FROM usuarioss WHERE usuario = ? AND contraseña = ? AND rol = ?";
            pst = cn.prepareStatement(sql);
            pst.setString(1, user);
            pst.setString(2, pass);
            pst.setString(3, rol);
            rs = pst.executeQuery();

            if (rs.next()) {
                accesoCorrecto = true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e);
        } finally {
            // Cerrar recursos en orden inverso (ResultSet -> PreparedStatement -> Connection)
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
                if (cn != null) cn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al cerrar conexión: " + e);
            }
        }

        return accesoCorrecto;
    }
}