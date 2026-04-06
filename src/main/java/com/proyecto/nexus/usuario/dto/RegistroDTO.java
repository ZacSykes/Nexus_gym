package com.proyecto.nexus.usuario.dto;

import lombok.Data;

@Data
public class RegistroDTO {
    private String nombre;
    private String apellido;
    private String cedula;
    private String telefono;
    private String correo;
    private String password;
    private String confirmarPassword;
    private String genero;
    private String fechaNacimiento;
    private String direccion;
    private Long gimnasioId;   // ← NUEVO CAMPO
}