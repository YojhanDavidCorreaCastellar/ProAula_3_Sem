/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import controller.Usuario;
import java.util.Date;

/**
 *
 * @author kenie
 */
public class Cliente extends Usuario{
    
    private String celular;
    private String userName;

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Cliente(String celular, String userName, int id_usuario, String nombre, String email, String contraseña, Date fecha_registro) {
        super(id_usuario, nombre, email, contraseña, fecha_registro);
        this.celular = celular;
        this.userName = userName;
    }
    
}


