package com.proyecto.nexus.usuario.dto;

import lombok.Data;

@Data
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String documento; // cédula
    private String rol;

}