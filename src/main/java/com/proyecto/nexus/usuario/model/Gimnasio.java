package com.proyecto.nexus.usuario.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "gimnasios")
public class Gimnasio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGimnasio;
    private String nit;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private String ciudad;
    private String horario;
    private String descripcion;
    private LocalDateTime createdAt;

    // Getters y Setters (generar con Lombok o manual)
}