package com.Proyecto.nexus.domain;

import lombok.Data;

@Data
public class Usuarios {
    private Long idUsuarios;   // mapea a datos_usuarios.id_usuario
    private String nombre;
    private String apellido;
    private String email;       // correo
    private String documento;   // cédula
    private String telefono;
    private String rol;         // ADMIN, INSTRUCTOR, USUARIO
}