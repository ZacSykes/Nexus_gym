package com.Proyecto.nexus.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "planes")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_planes")
    private Integer idPlanes;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "precio")
    private Double precio;

    @Column(name = "total_clases")
    private Integer totalClases;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "personas")
    private Integer personas;

    @Column(name = "estado")
    private String estado;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}