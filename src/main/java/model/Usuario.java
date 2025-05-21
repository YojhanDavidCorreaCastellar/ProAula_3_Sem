/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;


import java.util.Date;

public class Usuario {
    private int id_usuario;
    private String nombre;
    private String email;
    private String contraseña;
    private Date fecha_registro;

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public Date getFecha_registro() {
        return fecha_registro;
    }

    public void setFecha_registro(Date fecha_registro) {
        this.fecha_registro = fecha_registro;
    }

    public Usuario(int id_usuario, String nombre, String email, String contraseña, Date fecha_registro) {
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.email = email;
        this.contraseña = contraseña;
        this.fecha_registro = fecha_registro;
    }
    
    public Usuario() {
        
    }
 

    
}
