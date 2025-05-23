/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Random;
import javax.swing.JOptionPane;
import model.dbConect;
import org.json.JSONArray;
import org.json.JSONObject;
import view.pestañaMain;

/**
 *
 * @author YOJHAN DAVID
 */

public class pestañaReserva extends javax.swing.JFrame {
    
    private String codigoVueloActual;
    private pestañaMain parentWindow;
    private int idUsuarioActual;

public pestañaReserva(pestañaMain parent, String codigoVuelo) {
    this.parentWindow = parent;
    this.codigoVueloActual = codigoVuelo;
    
    initComponents();
    this.setLocationRelativeTo(null);
    this.idUsuarioActual = obtenerIdUsuarioActual();
    
    reservaId.setText(codigoVuelo);
    reservaId.setEditable(false);
    
    cargarDatosVuelo();
    generarAsientoAleatorio();
}

    pestañaReserva() {
    }
    
        private void cargarDatosVuelo() {
        try (Connection conn = dbConect.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                 "SELECT origen, destino, precio, fecha_hora, duracion, avion " +
                 "FROM vuelos WHERE codigo = ?")) {
            
            pst.setString(1, codigoVueloActual);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                origenField.setText(rs.getString("origen"));
                destinoField.setText(rs.getString("destino"));
                precioField.setText(String.format("$%,.2f", rs.getDouble("precio")));
                fechaField.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("fecha_hora")));
                duracionField.setText(rs.getString("duracion"));
                avionField.setText(rs.getString("avion"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar datos del vuelo: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarAsientoAleatorio() {
        Random random = new Random();
        int fila = random.nextInt(30) + 1;
        char letra = (char) (random.nextInt(6) + 'A');
        asientoField.setText(fila + String.valueOf(letra));
    }

    private void confirmarReserva() {
        String asiento = asientoField.getText().trim();
        
        if (asiento.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No se ha generado un asiento válido", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = dbConect.getConnection()) {
            conn.setAutoCommit(false);
            
            // 1. Verificar disponibilidad
            if (!verificarDisponibilidad(conn)) {
                conn.rollback();
                return;
            }
            
            // 2. Insertar reserva
            try (PreparedStatement pst = conn.prepareStatement(
                 "INSERT INTO reservas (numero_vuelo, id_user, asiento, nombre_cliente) VALUES (?, ?, ?, ?)")) {
    
                    pst.setString(1, codigoVueloActual);
                    pst.setInt(2, idUsuarioActual);
                    pst.setString(3, asiento);
                    pst.setString(4, obtenerNombreCliente(idUsuarioActual));
                
                int filasInsertadas = pst.executeUpdate();
                
                if (filasInsertadas == 0) {
                    throw new SQLException("No se pudo crear la reserva");
                }
            }
            
            try (PreparedStatement pst = conn.prepareStatement(
                "UPDATE vuelos SET pasajeros_registrados = pasajeros_registrados + 1 " +
                "WHERE codigo = ? AND pasajeros_registrados < capacidad")) {
                
                pst.setString(1, codigoVueloActual);
                int updated = pst.executeUpdate();
                
                if (updated == 0) {
                    throw new SQLException("No se pudo actualizar el contador de pasajeros");
                }
            }
            
            conn.commit();
            
            JOptionPane.showMessageDialog(this, 
                """
                Reserva confirmada exitosamente!
                Vuelo: """ + codigoVueloActual + "\n" +
                "Asiento: " + asiento + "\n" +
                "ID Usuario: " + idUsuarioActual,
                "Reserva Exitosa",
                JOptionPane.INFORMATION_MESSAGE);
            
            if (parentWindow != null) {
                parentWindow.refrescarTablaVuelos();
            }
            
            this.dispose();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al confirmar reserva: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean verificarDisponibilidad(Connection conn) throws SQLException {
        try (PreparedStatement pst = conn.prepareStatement(
            "SELECT capacidad, pasajeros_registrados FROM vuelos WHERE codigo = ?")) {
            
            pst.setString(1, codigoVueloActual);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                int capacidad = rs.getInt("capacidad");
                int pasajeros = rs.getInt("pasajeros_registrados");
                
                if (pasajeros >= capacidad) {
                    JOptionPane.showMessageDialog(this, 
                        "Vuelo completo. No hay asientos disponibles.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            return true;
        }
    }  
    
private int obtenerIdUsuarioActual() {
    try {
        String input = JOptionPane.showInputDialog(this,
            "Ingrese su NOMBRE DE USUARIO (ej: yojhancto):",
            "Autenticación",
            JOptionPane.QUESTION_MESSAGE);
        
        if (input == null || input.trim().isEmpty()) {
            throw new RuntimeException("Usuario no proporcionado");
        }
        
        // Buscar el ID basado en el nombre de usuario
        try (Connection conn = dbConect.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                 "SELECT id_usuario FROM usuarioss WHERE usuario = ?")) {
            
            pst.setString(1, input.trim());
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id_usuario"); // Asumiendo que cambiaste a tipo numérico
            }
            throw new RuntimeException("Usuario no encontrado");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Error: " + e.getMessage(),
            "Error de autenticación",
            JOptionPane.ERROR_MESSAGE);
        throw new RuntimeException("Autenticación fallida");
    }
}
        
private String obtenerNombreCliente(int idUsuario) throws SQLException {
    try (Connection conn = dbConect.getConnection();
         PreparedStatement pst = conn.prepareStatement(
             "SELECT usuario FROM usuarioss WHERE id_usuario = ?")) {
        
        pst.setInt(1, idUsuario);
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            return rs.getString("usuario"); // Usamos la columna 'usuario'
        }
        throw new SQLException("Usuario no encontrado");
    }
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        reservaId = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        origenField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        destinoField = new javax.swing.JTextField();
        precioField = new javax.swing.JTextField();
        confirmBtt = new javax.swing.JToggleButton();
        jLabel7 = new javax.swing.JLabel();
        fechaField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        duracionField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        avionField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        asientoField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel2.setFont(new java.awt.Font("JetBrains Mono", 0, 12)); // NOI18N
        jLabel2.setText("Ingrese el ID del vuelo nuevamente:");

        jLabel9.setFont(new java.awt.Font("JetBrains Mono", 0, 12)); // NOI18N
        jLabel9.setText("Precio");

        jLabel6.setFont(new java.awt.Font("JetBrains Mono", 0, 12)); // NOI18N
        jLabel6.setText("Origen");

        jLabel8.setFont(new java.awt.Font("JetBrains Mono", 0, 12)); // NOI18N
        jLabel8.setText("Destino");

        confirmBtt.setFont(new java.awt.Font("JetBrains Mono", 0, 12)); // NOI18N
        confirmBtt.setText("GENERAR FACTURA");
        confirmBtt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmBttActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("JetBrains Mono", 0, 12)); // NOI18N
        jLabel7.setText("Fecha / Hora");

        jLabel3.setFont(new java.awt.Font("JetBrains Mono", 0, 24)); // NOI18N
        jLabel3.setText("Factura");

        jLabel4.setFont(new java.awt.Font("JetBrains Mono", 0, 36)); // NOI18N
        jLabel4.setText("CaribeAirlines");

        jLabel10.setFont(new java.awt.Font("JetBrains Mono", 0, 12)); // NOI18N
        jLabel10.setText("Duracion");

        jLabel11.setFont(new java.awt.Font("JetBrains Mono", 0, 12)); // NOI18N
        jLabel11.setText("Avion");

        jLabel12.setFont(new java.awt.Font("JetBrains Mono", 0, 12)); // NOI18N
        jLabel12.setText("Asiento");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(reservaId, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addComponent(confirmBtt))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(origenField, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(destinoField, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(precioField, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 467, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 467, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(fechaField, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(duracionField, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(avionField, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel12)
                    .addComponent(asientoField, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(583, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(reservaId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(confirmBtt))
                        .addGap(21, 21, 21)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(origenField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(destinoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(precioField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fechaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(duracionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(avionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(asientoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(171, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    private void confirmBttActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmBttActionPerformed
        // TODO add your handling code here:
             if (codigoVueloActual == null || codigoVueloActual.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No se ha identificado correctamente el vuelo", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int opcion = JOptionPane.showConfirmDialog(this,
            "¿Confirmar reserva para el vuelo " + codigoVueloActual + "?\nAsiento: " + asientoField.getText(),
            "Confirmar Reserva",
            JOptionPane.YES_NO_OPTION);
        
        if (opcion == JOptionPane.YES_OPTION) {
            confirmarReserva();
        }
        
    }//GEN-LAST:event_confirmBttActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(pestañaReserva.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(pestañaReserva.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(pestañaReserva.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(pestañaReserva.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new pestañaReserva().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField asientoField;
    private javax.swing.JTextField avionField;
    private javax.swing.JToggleButton confirmBtt;
    private javax.swing.JTextField destinoField;
    private javax.swing.JTextField duracionField;
    private javax.swing.JTextField fechaField;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField origenField;
    private javax.swing.JTextField precioField;
    private javax.swing.JTextField reservaId;
    // End of variables declaration//GEN-END:variables

}
